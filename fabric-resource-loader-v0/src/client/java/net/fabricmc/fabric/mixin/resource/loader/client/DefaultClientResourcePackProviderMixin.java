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

package net.fabricmc.fabric.mixin.resource.loader.client;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.client.resource.loader.FabricWrappedVanillaResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPackSource.class)
public class DefaultClientResourcePackProviderMixin {
	/**
	 * Injects into the method which registers/creates vanilla built-in resource packs,
	 * and replaces the local {@link net.minecraft.server.packs.repository.Pack.ResourcesSupplier}
	 * instance with our custom wrapper that supports loading from mods.
	 */
	@ModifyArg(
			method = "createBuiltinPack(Ljava/lang/String;Lnet/minecraft/server/packs/repository/Pack$ResourcesSupplier;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/server/packs/repository/Pack;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/packs/repository/Pack;readMetaAndCreate(Ljava/lang/String;Lnet/minecraft/network/chat/Component;ZLnet/minecraft/server/packs/repository/Pack$ResourcesSupplier;Lnet/minecraft/server/packs/PackType;Lnet/minecraft/server/packs/repository/Pack$Position;Lnet/minecraft/server/packs/repository/PackSource;)Lnet/minecraft/server/packs/repository/Pack;"
			),
			index = 3
	)
	private Pack.ResourcesSupplier onCreateVanillaBuiltinResourcePack(String name, Component displayName, boolean alwaysEnabled,
																	  Pack.ResourcesSupplier packFactory, PackType type, Pack.Position position, PackSource source) {
		return factory -> new FabricWrappedVanillaResourcePack((AbstractPackResources) packFactory.open(name), getModResourcePacks(name));
	}

	/**
	 * {@return all baked-in mod resource packs that provide resources in the specified subPath}.
	 */
	private static List<ModResourcePack> getModResourcePacks(String subPath) {
		List<ModResourcePack> packs = new ArrayList<>();
		ModResourcePackUtil.appendModResourcePacks(packs, PackType.CLIENT_RESOURCES, subPath);
		return packs;
	}
}
