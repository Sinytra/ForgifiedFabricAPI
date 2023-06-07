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

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.Map;

import static net.minecraftforge.common.CreativeModeTabRegistry.getName;

public final class MinecraftItemGroups {
    public static final ResourceLocation BUILDING_BLOCKS_ID = new ResourceLocation("minecraft:building_blocks");
    public static final ResourceLocation COLOURED_BLOCKS_ID = new ResourceLocation("minecraft:colored_blocks");
    public static final ResourceLocation NATURAL_ID = new ResourceLocation("minecraft:natural");
    public static final ResourceLocation FUNCTIONAL_ID = new ResourceLocation("minecraft:functional");
    public static final ResourceLocation REDSTONE_ID = new ResourceLocation("minecraft:redstone");
    public static final ResourceLocation HOTBAR_ID = new ResourceLocation("minecraft:hotbar");
    public static final ResourceLocation SEARCH_ID = new ResourceLocation("minecraft:search");
    public static final ResourceLocation TOOLS_ID = new ResourceLocation("minecraft:tools");
    public static final ResourceLocation COMBAT_ID = new ResourceLocation("minecraft:combat");
    public static final ResourceLocation FOOD_AND_DRINK_ID = new ResourceLocation("minecraft:food_and_drink");
    public static final ResourceLocation INGREDIENTS_ID = new ResourceLocation("minecraft:ingredients");
    public static final ResourceLocation SPAWN_EGGS_ID = new ResourceLocation("minecraft:spawn_eggs");
    public static final ResourceLocation OP_ID = new ResourceLocation("minecraft:op");
    public static final ResourceLocation INVENTORY_ID = new ResourceLocation("minecraft:inventory");

    // Map of Forge CreativeModeTab ID to Fabric CreativeModeTabs ID
    public static final Map<ResourceLocation, ResourceLocation> FORGE_ID_MAP = new ImmutableMap.Builder<ResourceLocation, ResourceLocation>()
        .put(getName(CreativeModeTabs.BUILDING_BLOCKS), MinecraftItemGroups.BUILDING_BLOCKS_ID)
        .put(getName(CreativeModeTabs.COLORED_BLOCKS), MinecraftItemGroups.COLOURED_BLOCKS_ID)
        .put(getName(CreativeModeTabs.NATURAL_BLOCKS), MinecraftItemGroups.NATURAL_ID)
        .put(getName(CreativeModeTabs.FUNCTIONAL_BLOCKS), MinecraftItemGroups.FUNCTIONAL_ID)
        .put(getName(CreativeModeTabs.REDSTONE_BLOCKS), MinecraftItemGroups.REDSTONE_ID)
        .put(getName(CreativeModeTabs.TOOLS_AND_UTILITIES), MinecraftItemGroups.TOOLS_ID)
        .put(getName(CreativeModeTabs.COMBAT), MinecraftItemGroups.COMBAT_ID)
        .put(getName(CreativeModeTabs.FOOD_AND_DRINKS), MinecraftItemGroups.FOOD_AND_DRINK_ID)
        .put(getName(CreativeModeTabs.INGREDIENTS), MinecraftItemGroups.INGREDIENTS_ID)
        .put(getName(CreativeModeTabs.SPAWN_EGGS), MinecraftItemGroups.SPAWN_EGGS_ID)
        .build();
    // Map of CreativeModeTab to Fabric CreativeModeTabs ID
    public static final Map<CreativeModeTab, ResourceLocation> FABRIC_ID_MAP = new ImmutableMap.Builder<CreativeModeTab, ResourceLocation>()
            .put(CreativeModeTabs.HOTBAR, MinecraftItemGroups.HOTBAR_ID)
            .put(CreativeModeTabs.INVENTORY, MinecraftItemGroups.INVENTORY_ID)
            .put(CreativeModeTabs.OP_BLOCKS, MinecraftItemGroups.OP_ID)
            .put(CreativeModeTabs.SEARCH, MinecraftItemGroups.SEARCH_ID)
            .build();

    private MinecraftItemGroups() {
    }
}
