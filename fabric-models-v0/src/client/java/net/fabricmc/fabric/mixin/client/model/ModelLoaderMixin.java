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

package net.fabricmc.fabric.mixin.client.model;

import net.fabricmc.fabric.impl.client.model.ModelLoaderHooks;
import net.fabricmc.fabric.impl.client.model.ModelLoadingRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(ModelBakery.class)
public abstract class ModelLoaderMixin implements ModelLoaderHooks {
	// this is the first one
	@Final
	@Shadow
	public static ModelResourceLocation MISSING_MODEL_LOCATION;
	@Final
	@Shadow
	private Set<ResourceLocation> loadingStack;
	@Final
	@Shadow
	private Map<ResourceLocation, UnbakedModel> unbakedCache;

	private ModelLoadingRegistryImpl.LoaderInstance fabric_mlrLoaderInstance;

	@Shadow
	private void cacheAndQueueDependencies(ResourceLocation id, UnbakedModel unbakedModel) {
	}

	@Shadow
	private void loadModel(ResourceLocation id) {
	}

	@Inject(at = @At("HEAD"), method = "loadModel", cancellable = true)
	private void loadModelHook(ResourceLocation id, CallbackInfo ci) {
		UnbakedModel customModel = fabric_mlrLoaderInstance.loadModelFromVariant(id);

		if (customModel != null) {
			cacheAndQueueDependencies(id, customModel);
			ci.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "loadTopLevel")
	private void addModelHook(ModelResourceLocation id, CallbackInfo info) {
		if (id == MISSING_MODEL_LOCATION) {
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			fabric_mlrLoaderInstance = ModelLoadingRegistryImpl.begin((ModelBakery) (Object) this, resourceManager);
		}
	}

	@Inject(at = @At("RETURN"), method = "<init>")
	private void initFinishedHook(CallbackInfo info) {
		//noinspection ConstantConditions
		fabric_mlrLoaderInstance.finish();
	}

	@Override
	public UnbakedModel fabric_loadModel(ResourceLocation id) {
		if (!loadingStack.add(id)) {
			throw new IllegalStateException("Circular reference while loading " + id);
		}

		loadModel(id);
		loadingStack.remove(id);
		return unbakedCache.get(id);
	}
}
