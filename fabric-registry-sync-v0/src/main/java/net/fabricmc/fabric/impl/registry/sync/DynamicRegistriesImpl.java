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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class DynamicRegistriesImpl {
	private static final List<RegistryDataLoader.RegistryData<?>> DYNAMIC_REGISTRIES = new ArrayList<>();
	public static final Set<ResourceKey<?>> FABRIC_DYNAMIC_REGISTRY_KEYS = new HashSet<>();
	public static final Set<ResourceKey<? extends Registry<?>>> DYNAMIC_REGISTRY_KEYS = new HashSet<>();
	public static final Map<ResourceKey<? extends Registry<?>>, Codec<?>> NETWORK_CODECS = new HashMap<>();

	private DynamicRegistriesImpl() {
	}

	public static <T> RegistryDataLoader.RegistryData<T> register(ResourceKey<? extends Registry<T>> key, Codec<T> serverCodec) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(serverCodec, "Server codec cannot be null");

		if (DataPackRegistriesHooks.getDataPackRegistriesWithDimensions().anyMatch(d -> d.key().equals(key))
				|| DYNAMIC_REGISTRY_KEYS.contains(key)
		) {
			throw new IllegalArgumentException("Dynamic registry " + key + " has already been registered!");
		}

		var entry = new RegistryDataLoader.RegistryData<>(key, serverCodec, RegistryValidator.none());
		DYNAMIC_REGISTRIES.add(entry);
		FABRIC_DYNAMIC_REGISTRY_KEYS.add(key);
		return entry;
	}

	public static <T> void addSyncedRegistry(ResourceKey<? extends Registry<T>> key, Codec<T> clientCodec, DynamicRegistries.SyncOption... options) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(clientCodec, "Client codec cannot be null");
		Objects.requireNonNull(options, "Options cannot be null");

		NETWORK_CODECS.put(key, clientCodec);
		FABRIC_DYNAMIC_REGISTRY_KEYS.add(key);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	static void onNewDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
		for (RegistryDataLoader.RegistryData dynamicRegistry : DYNAMIC_REGISTRIES) {
			Codec networkCodec = NETWORK_CODECS.get(dynamicRegistry.key());
			event.dataPackRegistry(dynamicRegistry.key(), dynamicRegistry.elementCodec(), networkCodec);
		}
		DYNAMIC_REGISTRIES.clear();
	}
}
