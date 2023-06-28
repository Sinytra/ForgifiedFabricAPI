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

package net.fabricmc.fabric.impl.itemgroup;

import static net.minecraftforge.common.CreativeModeTabRegistry.getName;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Identifier;

public final class MinecraftItemGroups {
	public static final Identifier BUILDING_BLOCKS_ID = new Identifier("minecraft:building_blocks");
	public static final Identifier COLOURED_BLOCKS_ID = new Identifier("minecraft:colored_blocks");
	public static final Identifier NATURAL_ID = new Identifier("minecraft:natural");
	public static final Identifier FUNCTIONAL_ID = new Identifier("minecraft:functional");
	public static final Identifier REDSTONE_ID = new Identifier("minecraft:redstone");
	public static final Identifier HOTBAR_ID = new Identifier("minecraft:hotbar");
	public static final Identifier SEARCH_ID = new Identifier("minecraft:search");
	public static final Identifier TOOLS_ID = new Identifier("minecraft:tools");
	public static final Identifier COMBAT_ID = new Identifier("minecraft:combat");
	public static final Identifier FOOD_AND_DRINK_ID = new Identifier("minecraft:food_and_drink");
	public static final Identifier INGREDIENTS_ID = new Identifier("minecraft:ingredients");
	public static final Identifier SPAWN_EGGS_ID = new Identifier("minecraft:spawn_eggs");
	public static final Identifier OP_ID = new Identifier("minecraft:op");
	public static final Identifier INVENTORY_ID = new Identifier("minecraft:inventory");

	// Map of Forge CreativeModeTab ID to Fabric CreativeModeTabs ID
	public static final Map<Identifier, Identifier> FORGE_ID_MAP = new ImmutableMap.Builder<Identifier, Identifier>()
			.put(getName(ItemGroups.BUILDING_BLOCKS), MinecraftItemGroups.BUILDING_BLOCKS_ID)
			.put(getName(ItemGroups.COLORED_BLOCKS), MinecraftItemGroups.COLOURED_BLOCKS_ID)
			.put(getName(ItemGroups.NATURAL), MinecraftItemGroups.NATURAL_ID)
			.put(getName(ItemGroups.FUNCTIONAL), MinecraftItemGroups.FUNCTIONAL_ID)
			.put(getName(ItemGroups.REDSTONE), MinecraftItemGroups.REDSTONE_ID)
			.put(getName(ItemGroups.TOOLS), MinecraftItemGroups.TOOLS_ID)
			.put(getName(ItemGroups.COMBAT), MinecraftItemGroups.COMBAT_ID)
			.put(getName(ItemGroups.FOOD_AND_DRINK), MinecraftItemGroups.FOOD_AND_DRINK_ID)
			.put(getName(ItemGroups.INGREDIENTS), MinecraftItemGroups.INGREDIENTS_ID)
			.put(getName(ItemGroups.SPAWN_EGGS), MinecraftItemGroups.SPAWN_EGGS_ID)
			.build();
	// Map of CreativeModeTab to Fabric CreativeModeTabs ID
	public static final Map<ItemGroup, Identifier> FABRIC_ID_MAP = new ImmutableMap.Builder<ItemGroup, Identifier>()
			.put(ItemGroups.HOTBAR, MinecraftItemGroups.HOTBAR_ID)
			.put(ItemGroups.INVENTORY, MinecraftItemGroups.INVENTORY_ID)
			.put(ItemGroups.OPERATOR, MinecraftItemGroups.OP_ID)
			.put(ItemGroups.SEARCH, MinecraftItemGroups.SEARCH_ID)
			.build();

	private MinecraftItemGroups() {
	}
}
