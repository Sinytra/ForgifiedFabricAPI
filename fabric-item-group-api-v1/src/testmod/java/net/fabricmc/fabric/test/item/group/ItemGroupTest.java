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

package net.fabricmc.fabric.test.item.group;

import com.google.common.base.Supplier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

@Mod(ItemGroupTest.MOD_ID)
public class ItemGroupTest {
	public static final String MOD_ID = "fabric_item_group_api_v1_testmod";

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	private static final DeferredRegister<ItemGroup> ITEM_GROUPS = DeferredRegister.create(RegistryKeys.ITEM_GROUP, MOD_ID);

	private static final RegistryObject<Item> TEST_ITEM = ITEMS.register("item_test_group", () -> new Item(new Item.Settings()));

	static {
		//Adds an item group with all items in it
		ITEM_GROUPS.register("test_group", () -> FabricItemGroup.builder()
				.displayName(Text.literal("Test Item Group"))
				.icon(() -> new ItemStack(Items.DIAMOND))
				.entries((context, entries) -> {
					entries.addAll(Registries.ITEM.stream()
							.map(ItemStack::new)
							.filter(input -> !input.isEmpty())
							.toList());
				})
				.build());

		for (int i = 0; i < 100; i++) {
			final int index = i;

			ITEM_GROUPS.register("test_group_" + index, () -> FabricItemGroup.builder()
					.displayName(Text.literal("Test Item Group: " + index))
					.icon((Supplier<ItemStack>) () -> new ItemStack(Registries.BLOCK.get(index)))
					.entries((context, entries) -> {
						var itemStack = new ItemStack(Registries.ITEM.get(index));

						if (!itemStack.isEmpty()) {
							entries.add(itemStack);
						}
					})
					.build());
		}
	}

	public ItemGroupTest() {
		final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(bus);
		ITEM_GROUPS.register(bus);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register((content) -> {
			content.add(TEST_ITEM.get());

			content.addBefore(Blocks.OAK_FENCE, Items.DIAMOND, Items.DIAMOND_BLOCK);
			content.addAfter(Blocks.OAK_DOOR, Items.EMERALD, Items.EMERALD_BLOCK);

			// Test adding when the existing entry does not exist.
			content.addBefore(Blocks.BEDROCK, Items.GOLD_INGOT, Items.GOLD_BLOCK);
			content.addAfter(Blocks.BEDROCK, Items.IRON_INGOT, Items.IRON_BLOCK);
		});

		// Add a differently damaged pickaxe to all groups
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, content) -> {
			ItemStack minDmgPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
			minDmgPickaxe.setDamage(1);
			content.prepend(minDmgPickaxe);

			ItemStack maxDmgPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
			maxDmgPickaxe.setDamage(maxDmgPickaxe.getMaxDamage() - 1);
			content.add(maxDmgPickaxe);
		});

		try {
			// Test to make sure that item groups must have a display name.
			FabricItemGroup.builder().build();
			throw new AssertionError();
		} catch (IllegalStateException ignored) {
			// Ignored
		}
	}
}
