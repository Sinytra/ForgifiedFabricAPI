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

package net.fabricmc.fabric.test.transfer.ingame;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.test.transfer.TransferApiTests;

public class TransferTestInitializer {
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TransferApiTests.NAMESPACE);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TransferApiTests.NAMESPACE);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TransferApiTests.NAMESPACE);

	private static final RegistryObject<Block> INFINITE_WATER_SOURCE = BLOCKS.register("infinite_water_source", () -> new Block(AbstractBlock.Settings.of(Material.METAL)));
	private static final RegistryObject<Item> INFINITE_WATER_SOURCE_ITEM = ITEMS.register("infinite_water_source", () -> new BlockItem(INFINITE_WATER_SOURCE.get(), new Item.Settings()));

	private static final RegistryObject<Block> INFINITE_LAVA_SOURCE = BLOCKS.register("infinite_lava_source", () -> new Block(AbstractBlock.Settings.of(Material.METAL)));
	private static final RegistryObject<Item> INFINITE_LAVA_SOURCE_ITEM = ITEMS.register("infinite_lava_source", () -> new BlockItem(INFINITE_LAVA_SOURCE.get(), new Item.Settings()));

	private static final RegistryObject<Block> FLUID_CHUTE = BLOCKS.register("fluid_chute", FluidChuteBlock::new);
	private static final RegistryObject<Item> FLUID_CHUTE_ITEM = ITEMS.register("fluid_chute", () -> new BlockItem(FLUID_CHUTE.get(), new Item.Settings()));

	private static final RegistryObject<Block> ITEM_CHUTE = BLOCKS.register("item_chute", ItemChuteBlock::new);
	private static final RegistryObject<Item> ITEM_CHUTE_ITEM = ITEMS.register("item_chute", () -> new BlockItem(ITEM_CHUTE.get(), new Item.Settings()));

	private static final RegistryObject<Item> EXTRACT_STICK = ITEMS.register("extract_stick", ExtractStickItem::new);

	public static final RegistryObject<BlockEntityType<FluidChuteBlockEntity>> FLUID_CHUTE_TYPE = BLOCK_ENTITY_TYPES.register("fluid_chute", () -> FabricBlockEntityTypeBuilder.create(FluidChuteBlockEntity::new, FLUID_CHUTE.get()).build());
	public static final RegistryObject<BlockEntityType<ItemChuteBlockEntity>> ITEM_CHUTE_TYPE = BLOCK_ENTITY_TYPES.register("item_chute", () -> FabricBlockEntityTypeBuilder.create(ItemChuteBlockEntity::new, ITEM_CHUTE.get()).build());

	public static void onInitialize(IEventBus bus) {
		BLOCKS.register(bus);
		ITEMS.register(bus);
		BLOCK_ENTITY_TYPES.register(bus);
		bus.addListener(TransferTestInitializer::onCommonSetup);

		// Obsidian is now a trash can :-P
		ItemStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> TrashingStorage.ITEM, Blocks.OBSIDIAN);
		// And diamond ore blocks are an infinite source of diamonds! Yay!
		ItemStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> CreativeStorage.DIAMONDS, Blocks.DIAMOND_ORE);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		FluidStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> CreativeStorage.WATER, INFINITE_WATER_SOURCE.get());
		FluidStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> CreativeStorage.LAVA, INFINITE_LAVA_SOURCE.get());
		FluidStorage.SIDED.registerForBlockEntity((be, side) -> be.storage, FLUID_CHUTE_TYPE.get());
		ItemStorage.SIDED.registerForBlockEntity((be, side) -> be.storage, ITEM_CHUTE_TYPE.get());
	}
}
