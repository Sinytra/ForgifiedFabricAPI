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

package net.fabricmc.fabric.test.blockrenderlayer;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.Fluid;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@Mod(BlockRenderLayerTest.MODID)
public class BlockRenderLayerTest {
    public static final String MODID = "fabric_blockrenderlayer_v1_testmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);

    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(AbstractBlock.Settings.create()));
    public static final RegistryObject<Fluid> EXAMPLE_FLUID = FLUIDS.register("example_item", EmptyFluid::new);

    public BlockRenderLayerTest() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onClientSetup);
        BLOCKS.register(bus);
        FLUIDS.register(bus);

        ServerLifecycleEvents.SERVER_STARTED.register(client -> {
            if (!RenderLayers.getRenderLayers(EXAMPLE_BLOCK.get().getDefaultState()).contains(RenderLayer.getCutout())) {
                throw new AssertionError("Expected EXAMPLE_BLOCK to contain RenderType.cutout render type");
            }
            if (RenderLayers.getFluidLayer(EXAMPLE_FLUID.get().getDefaultState()) != RenderLayer.getTranslucent()) {
                throw new AssertionError("Expected render type of EXAMPLE_FLUID to be RenderType.translucent");
            }
            if (RenderLayers.getBlockLayer(EXAMPLE_BLOCK.get().getDefaultState()) != RenderLayer.getCutout()) {
				throw new AssertionError("Expected EXAMPLE_BLOCK to be RenderType.cutout");
            }

            // Success!
            LOGGER.info("The tests for block render type layers passed!");
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        BlockRenderLayerMap.INSTANCE.putBlock(EXAMPLE_BLOCK.get(), RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putFluid(EXAMPLE_FLUID.get(), RenderLayer.getTranslucent());
    }
}
