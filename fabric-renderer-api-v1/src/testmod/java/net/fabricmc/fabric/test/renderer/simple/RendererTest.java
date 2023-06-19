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

package net.fabricmc.fabric.test.renderer.simple;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.test.renderer.simple.client.RendererClientTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple testmod that renders a simple block rendered using the fabric renderer api.
 * The block that is rendered is a simple frame that another block is rendered in.
 * Blocks that provide a block entity cannot be placed inside the frame.
 *
 * <p>There are no fancy shaders or glow that is provided by this renderer test.
 */
@Mod(RendererTest.MODID)
public final class RendererTest {
    public static final String MODID = "fabric_renderer_api_v1_testmod";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final List<RegistryObject<FrameBlock>> FRAMES = new ArrayList<>();

    static {
        String[] frames = {"frame", "frame_multipart", "frame_weighted"};

        for (String name : frames) {
            RegistryObject<FrameBlock> block = BLOCKS.register(name, () -> new FrameBlock(id(name)));
            FRAMES.add(block);
            ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

    public static final RegistryObject<BlockEntityType<FrameBlockEntity>> FRAME_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("frame", () -> FabricBlockEntityTypeBuilder.create(FrameBlockEntity::new, FRAMES.stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    // To anyone testing this: pillars are supposed to connect vertically with each other.
    // Additionally, they should also connect vertically to frame blocks containing a pillar.
    // (The frame block will not change, but adjacent pillars should adjust their textures).
    public static final ResourceLocation PILLAR_ID = id("pillar");
    public static final RegistryObject<Block> PILLAR = BLOCKS.register(PILLAR_ID.getPath(), () -> new Block(FabricBlockSettings.of(Material.STONE)));
    public static final RegistryObject<Item> PILLAR_ITEM = ITEMS.register(PILLAR_ID.getPath(), () -> new BlockItem(PILLAR.get(), new Item.Properties()));

    public RendererTest() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        if (FMLLoader.getDist() == Dist.CLIENT) {
            bus.addListener(RendererClientTest::onInitializeClient);
        }
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
