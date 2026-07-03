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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import net.neoforged.neoforge.registries.callback.AddCallback;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.mixin.registry.sync.BaseMappedRegistryAccessor;
import net.fabricmc.fabric.mixin.registry.sync.MappedRegistryAccessor;
import net.fabricmc.fabric.mixin.registry.sync.RegistryManagerAccessor;

@SuppressWarnings({"unchecked", "rawtypes"})
public class FabricRegistryInit implements ModInitializer {
	private static final Map<Registry<?>, Event<RegistryEntryAddedCallback>> REGISTRY_ENTRY_ADDED_CALLBACKS = new ConcurrentHashMap<>();
	private static final Map<Registry<?>, Event<RegistryIdRemapCallback<?>>> REMAP_CALLBACKS = new HashMap<>();

	@Override
	public void onInitialize() {
		IEventBus bus = ModLoadingContext.get().getActiveContainer().getEventBus();
		bus.addListener(DataPackRegistryEvent.NewRegistry.class, DynamicRegistriesImpl::onNewDatapackRegistries);
		bus.addListener(ModifyRegistriesEvent.class, FabricRegistryInit::injectCallbacks);
	}

	public static void injectCallbacks(ModifyRegistriesEvent event) {
		event.getRegistries().forEach(registry -> {
			getRemapCallbackEvent(registry);
			registry.addCallback(new FapiRemapBridge<>());
		});
	}

	public static <T> Event<RegistryIdRemapCallback<T>> getRemapCallbackEvent(Registry<T> registry) {
		return (Event) REMAP_CALLBACKS.computeIfAbsent(registry, r -> EventFactory.createArrayBacked(RegistryIdRemapCallback.class,
				callbacks -> a -> {
					for (RegistryIdRemapCallback callback : callbacks) {
						callback.onRemap(a);
					}
				}
		));
	}

	public static <T> Event<RegistryEntryAddedCallback<T>> objectAddedEvent(Registry<T> registry) {
		return (Event<RegistryEntryAddedCallback<T>>) (Object) REGISTRY_ENTRY_ADDED_CALLBACKS.computeIfAbsent(registry, k -> {
			Event<RegistryEntryAddedCallback> event = EventFactory.createArrayBacked(RegistryEntryAddedCallback.class,
					callbacks -> (rawId, id, object) -> {
						for (RegistryEntryAddedCallback callback : callbacks) {
							callback.onEntryAdded(rawId, id, object);
						}
					}
			);
			k.addCallback(AddCallback.class, (reg, id, key, val) -> event.invoker().onEntryAdded(id, key.identifier(), val));
			return event;
		});
	}

	public static void addRegistry(Registry<?> registry) {
		RegistryManagerAccessor.invokeTrackModdedRegistry(registry.key().identifier());

		boolean frozen = ((MappedRegistryAccessor) BuiltInRegistries.REGISTRY).getFrozen();
		if (frozen) {
			((BaseMappedRegistryAccessor) BuiltInRegistries.REGISTRY).invokeUnfreeze(false);
		}

		((WritableRegistry) BuiltInRegistries.REGISTRY).register(registry.key(), registry, RegistrationInfo.BUILT_IN);

		if (frozen) {
			((WritableRegistry<?>) BuiltInRegistries.REGISTRY).freeze();
		}
	}
}
