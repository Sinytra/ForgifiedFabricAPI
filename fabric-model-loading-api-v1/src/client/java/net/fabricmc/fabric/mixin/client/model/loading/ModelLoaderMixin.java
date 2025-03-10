/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.client.model.loading;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.fabricmc.fabric.impl.client.model.loading.BakerImplHooks;
import net.fabricmc.fabric.impl.client.model.loading.BlockStatesLoaderHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoaderHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingConstants;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;

@Mixin(value = ModelBakery.class, priority = 2000)
abstract class ModelLoaderMixin implements ModelLoaderHooks {
	@Final
	@Shadow
	private Set<ResourceLocation> loadingStack;
	@Final
	@Shadow
	private Map<ResourceLocation, UnbakedModel> unbakedCache;
	@Shadow
	@Final
	private Map<ModelResourceLocation, UnbakedModel> topLevelModels;
	@Shadow
	@Final
	private UnbakedModel missingModel;

	@Unique
	private ModelLoadingEventDispatcher fabric_eventDispatcher;
	@Unique
	private final ObjectLinkedOpenHashSet<ResourceLocation> modelLoadingStack = new ObjectLinkedOpenHashSet<>();

	@Shadow
	abstract UnbakedModel getModel(ResourceLocation id);

	@Shadow
	abstract void registerModelAndLoadDependencies(ModelResourceLocation id, UnbakedModel unbakedModel);

	@Shadow
	abstract BlockModel loadBlockModel(ResourceLocation id);

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BlockStateModelLoader;loadAllBlockStates()V"))
	private void afterMissingModelInit(BlockColors blockColors, ProfilerFiller profiler, Map<ResourceLocation, BlockModel> jsonUnbakedModels, Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> blockStates, CallbackInfo info, @Local BlockStateModelLoader blockStatesLoader) {
		// Sanity check
		if (missingModel == null || !topLevelModels.containsKey(ModelBakery.MISSING_MODEL_VARIANT)) {
			throw new AssertionError("Missing model not initialized. This is likely a Fabric API porting bug.");
		}

		// Add the missing model to the cache since vanilla doesn't. Mods may load/bake the missing model directly.
		unbakedCache.put(ModelBakery.MISSING_MODEL_LOCATION, missingModel);
		profiler.popPush("fabric_plugins_init");

		fabric_eventDispatcher = new ModelLoadingEventDispatcher((ModelBakery) (Object) this, ModelLoadingPluginManager.CURRENT_PLUGINS.get());
		fabric_eventDispatcher.addExtraModels(this::addExtraModel);
		((BlockStatesLoaderHooks) blockStatesLoader).fabric_setLoadingOverride(fabric_eventDispatcher::loadBlockStateModels);
	}

	@Unique
	private void addExtraModel(ResourceLocation id) {
		ModelResourceLocation modelId = ModelLoadingConstants.toResourceModelId(id);
		UnbakedModel unbakedModel = getModel(id);
		registerModelAndLoadDependencies(modelId, unbakedModel);
	}

	@Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
	private void allowRecursiveLoading(ResourceLocation id, CallbackInfoReturnable<UnbakedModel> cir) {
		// If the stack is empty, this is the top-level call, so proceed as normal.
		if (!modelLoadingStack.isEmpty()) {
			if (unbakedCache.containsKey(id)) {
				cir.setReturnValue(unbakedCache.get(id));
			} else if (modelLoadingStack.contains(id)) {
				throw new IllegalStateException("Circular reference while loading model '" + id + "' (" + modelLoadingStack.stream().map(i -> i + "->").collect(Collectors.joining()) + id + ")");
			} else {
				UnbakedModel model = loadModel(id, null);
				unbakedCache.put(id, model);
				// These will be loaded at the top-level call.
				loadingStack.addAll(model.getDependencies());
				cir.setReturnValue(model);
			}
		}
	}

	@ModifyVariable(method = "getModel", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/resources/model/ModelBakery;loadBlockModel(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/block/model/BlockModel;"))
	private UnbakedModel doLoadModel(UnbakedModel model, @Local(ordinal = 1) ResourceLocation id) {
		return loadModel(id, model);
	}

	@Unique
	private UnbakedModel loadModel(ResourceLocation id, @Nullable UnbakedModel oldModel) {
		modelLoadingStack.add(id);

		try {
			UnbakedModel model = fabric_eventDispatcher.resolveModel(id);

			if (model == null) {
				if (oldModel == null) {
					model = loadBlockModel(id);
				} else {
					model = oldModel;
				}
			}

			return fabric_eventDispatcher.modifyModelOnLoad(model, id, null);
		} finally {
			modelLoadingStack.removeLast();
		}
	}

	@ModifyVariable(method = "registerModelAndLoadDependencies", at = @At("HEAD"), argsOnly = true)
	private UnbakedModel onAdd(UnbakedModel model, ModelResourceLocation id) {
		if (ModelLoadingConstants.isResourceModelId(id)) {
			return model;
		}

		return fabric_eventDispatcher.modifyModelOnLoad(model, null, id);
	}

	@WrapOperation(method = "lambda$bakeModels$6(Lnet/minecraft/client/resources/model/ModelBakery$TextureGetter;Lnet/minecraft/client/resources/model/ModelResourceLocation;Lnet/minecraft/client/resources/model/UnbakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery$ModelBakerImpl;bakeUncached(Lnet/minecraft/client/resources/model/UnbakedModel;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;"))
	private BakedModel wrapSingleOuterBake(@Coerce ModelBaker baker, UnbakedModel unbakedModel, ModelState settings, Operation<BakedModel> operation, ModelBakery.TextureGetter spriteGetter, ModelResourceLocation id) {
		if (ModelLoadingConstants.isResourceModelId(id) || id.equals(ModelBakery.MISSING_MODEL_VARIANT)) {
			// Call the baker instead of the operation to ensure the baked model is cached and doesn't end up going
			// through events twice.
			// This ignores the UnbakedModel in modelsToBake but it should be the same as the one in unbakedModels.
			return baker.bake(id.id(), settings);
		}

		Function<Material, TextureAtlasSprite> textureGetter = ((BakerImplHooks) baker).fabric_getTextureGetter();
		unbakedModel = fabric_eventDispatcher.modifyModelBeforeBake(unbakedModel, null, id, textureGetter, settings, baker);
		BakedModel model = operation.call(baker, unbakedModel, settings);
		return fabric_eventDispatcher.modifyModelAfterBake(model, null, id, unbakedModel, textureGetter, settings, baker);
	}

	@Override
	public ModelLoadingEventDispatcher fabric_getDispatcher() {
		return fabric_eventDispatcher;
	}

	@Override
	public UnbakedModel fabric_getMissingModel() {
		return missingModel;
	}

	@Override
	public UnbakedModel fabric_getOrLoadModel(ResourceLocation id) {
		return getModel(id);
	}

	@Override
	public void fabric_add(ModelResourceLocation id, UnbakedModel model) {
		registerModelAndLoadDependencies(id, model);
	}
}
