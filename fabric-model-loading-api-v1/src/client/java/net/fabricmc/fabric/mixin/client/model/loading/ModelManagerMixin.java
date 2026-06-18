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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelLoader;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;

@Mixin(ModelManager.class)
abstract class ModelManagerMixin implements FabricModelManager {
	@Unique
	@Nullable
	private volatile CompletableFuture<ModelLoadingEventDispatcher> eventDispatcherFuture;

	@Unique
	@Nullable
	private Map<ExtraModelKey<?>, ?> extraModels;

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T getModel(ExtraModelKey<T> key) {
		return extraModels == null ? null : (T) extraModels.get(key);
	}

	@Inject(method = "reload", at = @At("HEAD"))
	private void onHeadReload(PreparableReloadListener.SharedState sharedState, Executor prepareExecutor, PreparableReloadListener.PreparationBarrier synchronizer, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		eventDispatcherFuture = ModelLoadingPluginManager.preparePlugins(sharedState, prepareExecutor).thenApplyAsync(ModelLoadingEventDispatcher::new, prepareExecutor);
	}

	@ModifyReturnValue(method = "reload", at = @At("RETURN"))
	private CompletableFuture<Void> resetEventDispatcherFuture(CompletableFuture<Void> future) {
		return future.thenApplyAsync(v -> {
			eventDispatcherFuture = null;
			return v;
		});
	}

	@ModifyExpressionValue(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelManager;loadBlockModels(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<Map<Identifier, UnbakedModel>> hookModels(CompletableFuture<Map<Identifier, UnbakedModel>> modelsFuture) {
		return modelsFuture.thenCombine(eventDispatcherFuture, (models, eventDispatcher) -> eventDispatcher.modifyModelsOnLoad(models));
	}

	@ModifyExpressionValue(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BlockStateModelLoader;loadBlockStates(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<BlockStateModelLoader.LoadedModels> hookBlockStateModels(CompletableFuture<BlockStateModelLoader.LoadedModels> modelsFuture) {
		return modelsFuture.thenCombine(eventDispatcherFuture, (models, eventDispatcher) -> eventDispatcher.modifyBlockModelsOnLoad(models));
	}

	@ModifyArg(method = "reload", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal = 0), index = 0)
	private Function<Void, ?> hookModelCollect(Function<Void, CompletableFuture<?>> function) {
		return withModelDispatcher(function);
	}

	@ModifyArg(method = "reload", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenComposeAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal = 0), index = 0)
	private Function<Void, CompletableFuture<?>> hookModelBaking(Function<Void, CompletableFuture<?>> function) {
		return withModelDispatcher(function);
	}

	@Unique
	private <T, R> Function<T, R> withModelDispatcher(Function<T, R> function) {
		CompletableFuture<ModelLoadingEventDispatcher> future = eventDispatcherFuture;
		if (future == null) return function;
		return x -> {
			ModelLoadingEventDispatcher.CURRENT.set(future.join());

			try {
				return function.apply(x);
			} finally {
				ModelLoadingEventDispatcher.CURRENT.remove();
			}
		};
	}

	@Inject(
			method = "discoverModelDependencies(Ljava/util/Map;Lnet/minecraft/client/resources/model/BlockStateModelLoader$LoadedModels;Lnet/minecraft/client/resources/model/ClientItemInfoLoader$LoadedClientInfos;Lnet/neoforged/neoforge/client/model/standalone/StandaloneModelLoader$LoadedModels;)Lnet/minecraft/client/resources/model/ModelManager$ResolvedModels;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelDiscovery;resolve()Ljava/util/Map;")
	)
	private static void resolveExtraModels(
			Map<Identifier, UnbakedModel> modelMap, BlockStateModelLoader.LoadedModels stateDefinition, ClientItemInfoLoader.LoadedClientInfos loadedClientInfos, StandaloneModelLoader.LoadedModels standaloneModels, CallbackInfoReturnable<?> cir,
			@Local(name = "result") ModelDiscovery result
	) {
		// We know eventDispatcherFuture is available, as it is required by the item and block models (hookModels).
		ModelLoadingEventDispatcher eventDispatcher = ModelLoadingEventDispatcher.CURRENT.get();
		if (eventDispatcher != null) eventDispatcher.getExtraModels().values().forEach(result::addRoot);
	}

	@Inject(method = "apply", at = @At(value = "RETURN"))
	private void onReturnUpload(CallbackInfo ci, @Local(name = "bakedModels") ModelBakery.BakingResult bakedModels) {
		extraModels = ((BakedModelsHooks) (Object) bakedModels).fabric_getExtraModels();
	}
}
