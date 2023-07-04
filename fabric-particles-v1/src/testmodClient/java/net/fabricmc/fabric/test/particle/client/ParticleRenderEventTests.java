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

package net.fabricmc.fabric.test.particle.client;

import net.minecraft.registry.tag.FluidTags;

import net.fabricmc.fabric.api.client.particle.v1.ParticleRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.test.particle.ParticleTestSetup;
import net.fabricmc.fabric.test.particle.ParticleTintTestBlock;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ParticleRenderEventTests {

	public static void onInitializeClient(FMLClientSetupEvent event) {
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			if (tintIndex == 0) {
				return ((ParticleTintTestBlock) state.getBlock()).color;
			}

			return -1;
		}, ParticleTestSetup.ALWAYS_TINTED.get(), ParticleTestSetup.TINTED_OVER_WATER.get(), ParticleTestSetup.NEVER_TINTED.get());

		ParticleRenderEvents.ALLOW_BLOCK_DUST_TINT.register((state, world, pos) -> {
			if (state.isOf(ParticleTestSetup.NEVER_TINTED.get())) {
				return false;
			} else if (state.isOf(ParticleTestSetup.TINTED_OVER_WATER.get())) {
				return world.getFluidState(pos.down()).isIn(FluidTags.WATER);
			}

			return true;
		});
	}
}
