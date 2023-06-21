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

package net.fabricmc.fabric.test.client.rendering.fluid;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(FabricFluidRenderingTestMod.MODID)
public class FabricFluidRenderingTestMod {
	public static final String MODID = "fabric_rendering_fluids_v1_testmod";

	public FabricFluidRenderingTestMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		if (FMLLoader.getDist() == Dist.CLIENT) {
			bus.addListener(FabricFluidRenderingTestModClient::onInitializeClient);
		}
		TestFluids.init(bus);
	}
}
