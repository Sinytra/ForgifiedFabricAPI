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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class DynamicRegistriesImpl {
	private static final List<RegistryDataLoader.RegistryData<?>> WORLD_REGISTRIES = new ArrayList<>();
	private static final List<RegistryDataLoader.RegistryData<?>> BOOTSTRAPPING_REGISTRIES = new ArrayList<>();
	private static final Set<ResourceKey<? extends Registry<?>>> VANILLA_DYNAMIC_REGISTRY_KEYS;
	public static final Set<ResourceKey<? extends Registry<?>>> FABRIC_DYNAMIC_REGISTRY_KEYS = new HashSet<>();
	public static final Map<ResourceKey<? extends Registry<?>>, Codec<?>> NETWORK_CODECS = new HashMap<>();


	static {
		Set<ResourceKey<? extends Registry<?>>> vanillaDynamicRegistryKeys = new HashSet<>();

		for (RegistryDataLoader.RegistryData<?> worldgenEntry : RegistryDataLoader.WORLDGEN_REGISTRIES) {
			vanillaDynamicRegistryKeys.add(worldgenEntry.key());
		}

		for (RegistryDataLoader.RegistryData<?> dimensionEntry : RegistryDataLoader.DIMENSION_REGISTRIES) {
			vanillaDynamicRegistryKeys.add(dimensionEntry.key());
		}

		VANILLA_DYNAMIC_REGISTRY_KEYS = Collections.unmodifiableSet(vanillaDynamicRegistryKeys);
	}

	private DynamicRegistriesImpl() {
	}

	private static void addDynamicRegistryData(ResourceKey<? extends Registry<?>> key, RegistryDataLoader.RegistryData<?> data) {
		FABRIC_DYNAMIC_REGISTRY_KEYS.add(key);
		BOOTSTRAPPING_REGISTRIES.add(data);
		WORLD_REGISTRIES.add(data);
	}

	public static <T> RegistryDataLoader.RegistryData<T> register(ResourceKey<? extends Registry<T>> key, Codec<T> serverCodec) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(serverCodec, "Server codec cannot be null");

		if (VANILLA_DYNAMIC_REGISTRY_KEYS.contains(key) || FABRIC_DYNAMIC_REGISTRY_KEYS.contains(key)) {
			throw new IllegalArgumentException("Dynamic registry " + key + " has already been registered!");
		}

		var entry = new RegistryDataLoader.RegistryData<>(key, serverCodec, RegistryValidator.none());
		addDynamicRegistryData(key, entry);
		return entry;
	}

	public static <T> void addSyncedRegistry(ResourceKey<? extends Registry<T>> key, Codec<T> networkCodec, DynamicRegistries.SyncOption... options) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(networkCodec, "Network codec cannot be null");
		Objects.requireNonNull(options, "Options cannot be null");

        NETWORK_CODECS.put(key, networkCodec);
		FABRIC_DYNAMIC_REGISTRY_KEYS.add(key);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
    static void onNewDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        for (RegistryDataLoader.RegistryData dynamicRegistry : WORLD_REGISTRIES) {
            Codec networkCodec = NETWORK_CODECS.get(dynamicRegistry.key());
            event.dataPackRegistry(dynamicRegistry.key(), dynamicRegistry.elementCodec(), networkCodec);
        }

		for (RegistryDataLoader.RegistryData dynamicRegistry : BOOTSTRAPPING_REGISTRIES) {
			Codec networkCodec = NETWORK_CODECS.get(dynamicRegistry.key());
			event.dataPackRegistry(dynamicRegistry.key(), dynamicRegistry.elementCodec(), networkCodec);
		}
		WORLD_REGISTRIES.clear();
		BOOTSTRAPPING_REGISTRIES.clear();
	}
}
