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

package net.fabricmc.fabric.api.particle.v1;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Methods for creating particle types, both simple and using an existing attribute factory.
 *
 * <p>Usage:
 * <blockquote>
 * <pre>
 * public static final DefaultParticleType SIMPLE_TEST_PARTICLE = FabricParticleTypes.simple();
 * public static final DefaultParticleType CUSTOM_TEST_PARTICLE = FabricParticleTypes.simple();
 *
 * {@literal @}Override
 * public void onInitialize() {
 *     Registry.register(Registry.PARTICLE_TYPE, new Identifier("testmod", "simple"), SIMPLE_TEST_PARTICLE);
 *     Registry.register(Registry.PARTICLE_TYPE, new Identifier("testmod", "custom"), CUSTOM_TEST_PARTICLE);
 * }}
 * </pre>
 * </blockquote>
 */
public final class FabricParticleTypes {
	private FabricParticleTypes() { }

	/**
	 * Creates a new, default particle type for the given id.
	 */
	public static SimpleParticleType simple() {
		return simple(false);
	}

	/**
	 * Creates a new, default particle type for the given id.
	 *
	 * @param alwaysSpawn True to always spawn the particle regardless of distance.
	 */
	public static SimpleParticleType simple(boolean alwaysSpawn) {
		return new SimpleParticleType(alwaysSpawn) { };
	}

	/**
	 * Creates a new particle type with a custom factory for packet/data serialization.
	 *
	 * @param factory	 A factory for serializing packet data and string command parameters into a particle effect.
	 */
	public static <T extends ParticleOptions> ParticleType<T> complex(ParticleOptions.Deserializer<T> factory) {
		return complex(false, factory);
	}

	/**
	 * Creates a new particle type with a custom factory for packet/data serialization.
	 *
	 * @param alwaysSpawn True to always spawn the particle regardless of distance.
	 * @param factory	 A factory for serializing packet data and string command parameters into a particle effect.
	 */
	public static <T extends ParticleOptions> ParticleType<T> complex(boolean alwaysSpawn, ParticleOptions.Deserializer<T> factory) {
		return new ParticleType<>(alwaysSpawn, factory) {
			@Override
			public Codec<T> codec() {
				//TODO fix me
				return null;
			}
		};
	}
}
