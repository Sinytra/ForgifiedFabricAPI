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

package net.fabricmc.fabric.impl.client.particle;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.client.core.registries.ClientRegistries;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

public final class ParticleProviderRegistryImpl implements ParticleProviderRegistry {
	public static final ParticleProviderRegistryImpl INSTANCE = new ParticleProviderRegistryImpl();

	static class DeferredParticleProviderRegistry implements ParticleProviderRegistry {
		private final Map<ParticleType<?>, ParticleProvider<?>> factories = new IdentityHashMap<>();
		private final Map<ParticleType<?>, PendingParticleProvider<?>> constructors = new IdentityHashMap<>();

		@Override
		public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider) {
			factories.put(type, provider);
		}

		@Override
		public <T extends ParticleOptions> void register(ParticleType<T> type, PendingParticleProvider<T> factory) {
			constructors.put(type, factory);
		}

		@SuppressWarnings("unchecked")
		void applyTo(ParticleProviderRegistry registry) {
			for (Map.Entry<ParticleType<?>, ParticleProvider<?>> entry : factories.entrySet()) {
				ParticleType type = entry.getKey();
				ParticleProvider factory = entry.getValue();
				registry.register(type, factory);
			}

			for (Map.Entry<ParticleType<?>, PendingParticleProvider<?>> entry : constructors.entrySet()) {
				ParticleType type = entry.getKey();
				PendingParticleProvider constructor = entry.getValue();
				registry.register(type, constructor);
			}
		}
	}

	static class DirectParticleProviderRegistry implements ParticleProviderRegistry {
		@Override
		public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider) {
			Registry.register(BuiltInClientRegistries.PARTICLE_PROVIDER, key(type), provider);
		}

		@Override
		public <T extends ParticleOptions> void register(ParticleType<T> type, PendingParticleProvider<T> constructor) {
			var delegate = new ParticleResources.MutableSpriteSet();
			var fabricSpriteSet = new FabricSpriteSetImpl(delegate);
			ParticleResources.SPRITE_SETS.put(key(type), delegate);
			register(type, constructor.create(fabricSpriteSet));
		}

		private static ResourceKey<ParticleProvider<?>> key(ParticleType<?> type) {
			return ResourceKey.create(ClientRegistries.PARTICLE_PROVIDER, BuiltInRegistries.PARTICLE_TYPE.getKey(type));
		}
	}

	ParticleProviderRegistry internalRegistry = new DeferredParticleProviderRegistry();

	private ParticleProviderRegistryImpl() { }

	@Override
	public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider) {
		internalRegistry.register(type, provider);
	}

	@Override
	public <T extends ParticleOptions> void register(ParticleType<T> type, PendingParticleProvider<T> constructor) {
		internalRegistry.register(type, constructor);
	}

	public void initialize() {
		ParticleProviderRegistry newRegistry = new DirectParticleProviderRegistry();
		DeferredParticleProviderRegistry oldRegistry = (DeferredParticleProviderRegistry) internalRegistry;
		oldRegistry.applyTo(newRegistry);
		internalRegistry = newRegistry;
	}
}
