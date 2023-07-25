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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import net.minecraftforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class DynamicRegistriesImpl {
	private static final List<DataPackRegistryData<?>> DYNAMIC_REGISTRIES = new ArrayList<>();
	public static final Set<RegistryKey<?>> FABRIC_DYNAMIC_REGISTRY_KEYS = new HashSet<>();
	public static final Set<RegistryKey<? extends Registry<?>>> SKIP_EMPTY_SYNC_REGISTRIES = new HashSet<>();

	private DynamicRegistriesImpl() {
	}

	public static @Unmodifiable List<RegistryLoader.Entry<?>> getDynamicRegistries() {
		return DataPackRegistriesHooks.getDataPackRegistries();
	}

	public static <T> void register(RegistryKey<? extends Registry<T>> key, Codec<T> codec) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(codec, "Codec cannot be null");

		DYNAMIC_REGISTRIES.add(new DataPackRegistryData<>(key, codec, null));
		FABRIC_DYNAMIC_REGISTRY_KEYS.add(key);
	}

	public static <T> void addSyncedRegistry(RegistryKey<? extends Registry<T>> registryKey, Codec<T> dataCodec, Codec<T> networkCodec, DynamicRegistries.SyncOption... options) {
		Objects.requireNonNull(registryKey, "Registry key cannot be null");
		Objects.requireNonNull(dataCodec, "Data codec cannot be null");
		Objects.requireNonNull(networkCodec, "Network codec cannot be null");
		Objects.requireNonNull(options, "Options cannot be null");

		for (DynamicRegistries.SyncOption option : options) {
			if (option == DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY) {
				SKIP_EMPTY_SYNC_REGISTRIES.add(registryKey);
			}
		}

		DYNAMIC_REGISTRIES.add(new DataPackRegistryData<>(registryKey, dataCodec, networkCodec));
		FABRIC_DYNAMIC_REGISTRY_KEYS.add(registryKey);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	static void onNewDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
		for (DataPackRegistryData dynamicRegistry : DYNAMIC_REGISTRIES) {
			event.dataPackRegistry(dynamicRegistry.key(), dynamicRegistry.codec(), dynamicRegistry.networkCodec());
		}
	}

	record DataPackRegistryData<T>(RegistryKey<? extends Registry<T>> key, Codec<T> codec, @Nullable Codec<T> networkCodec) {}
}
