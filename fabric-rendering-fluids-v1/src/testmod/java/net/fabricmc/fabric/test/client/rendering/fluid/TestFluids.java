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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TestFluids {
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FabricFluidRenderingTestMod.MODID);
	private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, FabricFluidRenderingTestMod.MODID);
	
	public static final RegistryObject<NoOverlayFluid> NO_OVERLAY = FLUIDS.register("no_overlay", NoOverlayFluid.Still::new);
	public static final RegistryObject<NoOverlayFluid> NO_OVERLAY_FLOWING = FLUIDS.register("no_overlay_flowing", NoOverlayFluid.Flowing::new);
	
	public static final RegistryObject<LiquidBlock> NO_OVERLAY_BLOCK = BLOCKS.register("no_overlay", () -> new LiquidBlock(NO_OVERLAY, BlockBehaviour.Properties.copy(Blocks.WATER)) {});

	public static final RegistryObject<OverlayFluid> OVERLAY = FLUIDS.register("overlay", OverlayFluid.Still::new);
	public static final RegistryObject<OverlayFluid> OVERLAY_FLOWING = FLUIDS.register("overlay_flowing", OverlayFluid.Flowing::new);
	
	public static final RegistryObject<LiquidBlock> OVERLAY_BLOCK = BLOCKS.register("overlay", () -> new LiquidBlock(OVERLAY, BlockBehaviour.Properties.copy(Blocks.WATER)) {});

	public static final RegistryObject<UnregisteredFluid> UNREGISTERED = FLUIDS.register("unregistered", UnregisteredFluid.Still::new);
	public static final RegistryObject<UnregisteredFluid> UNREGISTERED_FLOWING = FLUIDS.register("unregistered_flowing", UnregisteredFluid.Flowing::new);

	public static final RegistryObject<LiquidBlock> UNREGISTERED_BLOCK = BLOCKS.register("unregistered", () -> new LiquidBlock(UNREGISTERED, BlockBehaviour.Properties.copy(Blocks.WATER)) {});

	public static final RegistryObject<CustomFluid> CUSTOM = FLUIDS.register("custom", CustomFluid.Still::new);
	public static final RegistryObject<CustomFluid> CUSTOM_FLOWING = FLUIDS.register("custom_flowing", CustomFluid.Flowing::new);

	public static final RegistryObject<LiquidBlock> CUSTOM_BLOCK = BLOCKS.register("custom", () -> new LiquidBlock(CUSTOM, BlockBehaviour.Properties.copy(Blocks.WATER)) {});
	
	public static void init(IEventBus bus) {
		BLOCKS.register(bus);
		FLUIDS.register(bus);
	}
}
