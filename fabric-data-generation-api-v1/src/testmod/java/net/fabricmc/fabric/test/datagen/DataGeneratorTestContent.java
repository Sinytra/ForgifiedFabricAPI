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

package net.fabricmc.fabric.test.datagen;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

@Mod(DataGeneratorTestContent.MOD_ID)
public class DataGeneratorTestContent {
	public static final String MOD_ID = "fabric_data_gen_api_v1_testmod";

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	private static final DeferredRegister<ItemGroup> ITEM_GROUPS = DeferredRegister.create(RegistryKeys.ITEM_GROUP, MOD_ID);

	public static final RegistryObject<Block> SIMPLE_BLOCK = register("simple_block", true, AbstractBlock.Settings.create());
	public static final RegistryObject<Block> BLOCK_WITHOUT_ITEM = register("block_without_item", false, AbstractBlock.Settings.create());
	public static final RegistryObject<Block> BLOCK_WITHOUT_LOOT_TABLE = register("block_without_loot_table", false, AbstractBlock.Settings.create());
	public static final RegistryObject<Block> BLOCK_WITH_VANILLA_LOOT_TABLE = register("block_with_vanilla_loot_table", false, AbstractBlock.Settings.create().dropsLike(Blocks.STONE));
	public static final RegistryObject<Block> BLOCK_THAT_DROPS_NOTHING = register("block_that_drops_nothing", false, AbstractBlock.Settings.create().dropsNothing());

	public static final RegistryObject<ItemGroup> SIMPLE_ITEM_GROUP = ITEM_GROUPS.register("simple", () -> FabricItemGroup.builder()
			.icon(() -> new ItemStack(Items.DIAMOND_PICKAXE))
			.displayName(Text.translatable("fabric-data-gen-api-v1-testmod.simple_item_group"))
			.build());

	public DataGeneratorTestContent() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		BLOCKS.register(bus);
		ITEMS.register(bus);
		ITEM_GROUPS.register(bus);
		bus.addListener(DataGeneratorTestEntrypoint::onGatherData);

		ItemGroupEvents.modifyEntriesEvent(SIMPLE_ITEM_GROUP.getKey()).register(entries -> entries.add(SIMPLE_BLOCK.get()));
	}

	private static RegistryObject<Block> register(String name, boolean hasItem, AbstractBlock.Settings settings) {
		RegistryObject<Block> block = BLOCKS.register(name, () -> new Block(settings));
		if (hasItem) {
			ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Settings()));
		}
		return block;
	}
}
