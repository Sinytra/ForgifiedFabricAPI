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

package net.fabricmc.fabric.test.renderer;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RendererTest.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RendererTest.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RendererTest.MODID);

    public static final RegistryObject<FrameBlock> FRAME_BLOCK = registerBlock("frame", () -> new FrameBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).nonOpaque()));
    public static final RegistryObject<FrameBlock> FRAME_MULTIPART_BLOCK = registerBlock("frame_multipart", () -> new FrameBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).nonOpaque()));
    public static final RegistryObject<FrameBlock> FRAME_VARIANT_BLOCK = registerBlock("frame_variant", () -> new FrameBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).nonOpaque()));
    public static final RegistryObject<Block> PILLAR_BLOCK = registerBlock("pillar", () -> new Block(FabricBlockSettings.create()));
    public static final RegistryObject<Block> OCTAGONAL_COLUMN_BLOCK = registerBlock("octagonal_column", () -> new Block(FabricBlockSettings.create().nonOpaque().strength(1.8F)));

    public static final RegistryObject<Item> FRAME_ITEM = registerItem("frame", () -> new BlockItem(FRAME_BLOCK.get(), new Item.Settings()));
    public static final RegistryObject<Item> FRAME_MULTIPART_ITEM = registerItem("frame_multipart", () -> new BlockItem(FRAME_MULTIPART_BLOCK.get(), new Item.Settings()));
    public static final RegistryObject<Item> FRAME_VARIANT_ITEM = registerItem("frame_variant", () -> new BlockItem(FRAME_VARIANT_BLOCK.get(), new Item.Settings()));
    public static final RegistryObject<Item> PILLAR_ITEM = registerItem("pillar", () -> new BlockItem(PILLAR_BLOCK.get(), new Item.Settings()));
    public static final RegistryObject<Item> OCTAGONAL_COLUMN_ITEM = registerItem("octagonal_column", () -> new BlockItem(OCTAGONAL_COLUMN_BLOCK.get(), new Item.Settings()));

    public static final RegistryObject<BlockEntityType<FrameBlockEntity>> FRAME_BLOCK_ENTITY_TYPE = registerBlockEntity("frame", () -> FabricBlockEntityTypeBuilder.create(FrameBlockEntity::new, getFrameBlocks()).build(null));

    public static FrameBlock[] getFrameBlocks() {
        return new FrameBlock[]{
                FRAME_BLOCK.get(),
                FRAME_MULTIPART_BLOCK.get(),
                FRAME_VARIANT_BLOCK.get(),
        };
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String path, Supplier<T> block) {
        return BLOCKS.register(path, block);
    }

    private static <T extends Item> RegistryObject<T> registerItem(String path, Supplier<T> item) {
        return ITEMS.register(path, item);
    }

    private static <T extends BlockEntityType<?>> RegistryObject<T> registerBlockEntity(String path, Supplier<T> blockEntityType) {
        return BLOCK_ENTITY_TYPES.register(path, blockEntityType);
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        ITEMS.register(bus);
    }
}
