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

package net.fabricmc.fabric.mixin.registry.sync;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistryViewImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Mixin(RegistryDataLoader.class)
public class RegistryLoaderMixin {
	@Inject(
			method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
					ordinal = 0
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void beforeLoad(ResourceManager resourceManager, RegistryAccess baseRegistryManager, List<RegistryDataLoader.RegistryData<?>> entries, CallbackInfoReturnable<RegistryAccess.Frozen> cir, Map a, List<Pair<WritableRegistry<?>, ?>> registriesList, RegistryOps.RegistryInfoLookup registryManager) {
		Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries = new IdentityHashMap<>(registriesList.size());

		for (Pair<WritableRegistry<?>, ?> pair : registriesList) {
			registries.put(pair.getFirst().key(), pair.getFirst());
		}

		DynamicRegistrySetupCallback.EVENT.invoker().onRegistrySetup(new DynamicRegistryViewImpl(registries));
	}
}
