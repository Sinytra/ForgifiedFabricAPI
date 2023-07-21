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

package net.fabricmc.fabric.impl.registry.sync;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Lifecycle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.impl.client.registry.sync.FabricRegistryClientInit;
import net.fabricmc.fabric.mixin.registry.sync.RegistriesAccessor;

@Mod("fabric_registry_sync_v0")
public class FabricRegistryInit {
	private static final List<MutableRegistry<?>> REGISTRIES = new ArrayList<>();

	public FabricRegistryInit() {
		if (FMLLoader.getDist() == Dist.CLIENT) {
			FabricRegistryClientInit.onInitializeClient();
		}
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(DynamicRegistriesImpl::onNewDatapackRegistries);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				RegistrySyncManager.sendPacket(server, handler.player));
	}

	public static void addRegistry(MutableRegistry<?> registry) {
		REGISTRIES.add(registry);
	}

	public static void submitRegistries() {
		if (Registries.REGISTRIES instanceof SimpleRegistry<?> rootRegistry)
			rootRegistry.unfreeze();

		REGISTRIES.forEach(registry -> RegistriesAccessor.getROOT().add((RegistryKey<MutableRegistry<?>>) registry.getKey(), registry, Lifecycle.stable()));

		if (Registries.REGISTRIES instanceof SimpleRegistry<?> rootRegistry)
			rootRegistry.freeze();
	}
}
