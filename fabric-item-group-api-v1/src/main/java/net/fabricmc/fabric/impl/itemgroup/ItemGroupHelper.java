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

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.mixin.itemgroup.CreativeModeTabRegistryAccessor;

import net.minecraft.item.ItemGroups;

import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.event.CreativeModeTabEvent;

import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public final class ItemGroupHelper {
	private static final List<ItemGroup> TABS = new ArrayList<>();

	public static void appendItemGroup(ItemGroup tab) {
		TABS.add(tab);
	}

	static void registerCreativeTabs(CreativeModeTabEvent.Register event) {
		TABS.forEach(ItemGroupHelper::registerCreativeModeTab);
	}

	private static void registerCreativeModeTab(ItemGroup tab) {
		Identifier name = tab.getId();
		if (CreativeModeTabRegistry.getTab(name) != null)
			throw new IllegalStateException("Duplicate creative mode tab with name: " + name);

		if (tab.isSpecial())
			throw new IllegalStateException("CreativeModeTab " + name + " is aligned right, this is not supported!");

		CreativeModeTabRegistryAccessor.callProcessCreativeModeTab(tab, name, List.of(ItemGroups.SPAWN_EGGS), List.of());
	}
}
