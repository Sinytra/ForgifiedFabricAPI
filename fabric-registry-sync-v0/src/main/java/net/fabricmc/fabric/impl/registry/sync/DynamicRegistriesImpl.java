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
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.mixin.registry.sync.DataPackRegistryEventNewRegistryAccessor;

public final class DynamicRegistriesImpl {
	private static final List<PendingRegistry<?>> PENDING_REGISTRIES = new ArrayList<>();
	public static final Set<ResourceKey<?>> FABRIC_DYNAMIC_REGISTRY_KEYS = new HashSet<>();
	
	private static boolean initialized;

	private DynamicRegistriesImpl() {
	}

	public static <T> void register(ResourceKey<? extends Registry<T>> key, Codec<T> serverCodec) {
		register(key, serverCodec, null);
	}

	public static <T> void registerSynced(ResourceKey<? extends Registry<T>> key, Codec<T> serverCodec, Codec<T> clientCodec, DynamicRegistries.SyncOption... options) {
		Objects.requireNonNull(clientCodec, "Client codec cannot be null");
		Objects.requireNonNull(options, "Options cannot be null");

		register(key, serverCodec, clientCodec);
	}

	private static synchronized <T> void register(ResourceKey<? extends Registry<T>> key, Codec<T> serverCodec, @Nullable Codec<T> networkCodec) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(serverCodec, "Server codec cannot be null");

		if (DataPackRegistriesHooks.getDataPackRegistriesWithDimensions().anyMatch(d -> d.key().equals(key))
				|| FABRIC_DYNAMIC_REGISTRY_KEYS.contains(key)
		) {
			throw new IllegalArgumentException("Dynamic registry " + key + " has already been registered!");
		}

		FABRIC_DYNAMIC_REGISTRY_KEYS.add(key);
		PendingRegistry<T> registry = new PendingRegistry<>(key, serverCodec, networkCodec);

		if (initialized) {
			DataPackRegistryEvent.NewRegistry event = new DataPackRegistryEvent.NewRegistry();
			registry.register(event);
			((DataPackRegistryEventNewRegistryAccessor) (Object) event).invokeProcess();
		} else {
			PENDING_REGISTRIES.add(registry);
		}
	}

	static synchronized void onNewDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
		for (PendingRegistry<?> registry : PENDING_REGISTRIES) {
			registry.register(event);
		}

		PENDING_REGISTRIES.clear();
		initialized = true;
	}

	private record PendingRegistry<T>(ResourceKey<? extends Registry<T>> key, Codec<T> serverCodec, @Nullable Codec<T> networkCodec) {
		@SuppressWarnings("unchecked")
		void register(DataPackRegistryEvent.NewRegistry event) {
			event.dataPackRegistry((ResourceKey<Registry<T>>) this.key, this.serverCodec, this.networkCodec);
		}
	}
}
