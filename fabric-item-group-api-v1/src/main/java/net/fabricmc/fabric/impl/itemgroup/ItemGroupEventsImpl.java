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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.util.MutableHashedLinkedMap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ItemGroupEventsImpl {
	private static final Map<RegistryKey<ItemGroup>, Event<ItemGroupEvents.ModifyEntries>> ITEM_GROUP_EVENT_MAP = new HashMap<>();

	public static Event<ItemGroupEvents.ModifyEntries> getOrCreateModifyEntriesEvent(RegistryKey<ItemGroup> registryKey) {
		return ITEM_GROUP_EVENT_MAP.computeIfAbsent(registryKey, (g -> createModifyEvent()));
	}

	@Nullable
	public static Event<ItemGroupEvents.ModifyEntries> getModifyEntriesEvent(RegistryKey<ItemGroup> registryKey) {
		return ITEM_GROUP_EVENT_MAP.get(registryKey);
	}

	private static Event<ItemGroupEvents.ModifyEntries> createModifyEvent() {
		return EventFactory.createArrayBacked(ItemGroupEvents.ModifyEntries.class, callbacks -> (entries) -> {
			for (ItemGroupEvents.ModifyEntries callback : callbacks) {
				callback.modifyEntries(entries);
			}
		});
	}

	public static void onCreativeModeTabBuildContents(BuildCreativeModeTabContentsEvent event) {
		ItemGroup tab = event.getTab();
		ItemGroup.DisplayContext context = event.getParameters();

		List<ItemStack> displayStacks = new ArrayList<>();
		List<ItemStack> searchTabStacks = new ArrayList<>();
		MutableHashedLinkedMap<ItemStack, ItemGroup.StackVisibility> entries = event.getEntries();
		entries.forEach(entry -> {
			ItemStack stack = entry.getKey();
			ItemGroup.StackVisibility visibility = entry.getValue();
			if (visibility == ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS || visibility == ItemGroup.StackVisibility.PARENT_TAB_ONLY) {
				displayStacks.add(stack);
			}
			if (visibility == ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS || visibility == ItemGroup.StackVisibility.SEARCH_TAB_ONLY) {
				searchTabStacks.add(stack);
			}
		});

		FabricItemGroupEntries fabricEntries = new FabricItemGroupEntries(context, displayStacks, searchTabStacks);

		Event<ItemGroupEvents.ModifyEntries> modifyEntriesEvent = getModifyEntriesEvent(event.getTabKey());
		if (modifyEntriesEvent != null) {
			modifyEntriesEvent.invoker().modifyEntries(fabricEntries);
		}

		// Now trigger the global event
		if (event.getTabKey() != ItemGroups.OPERATOR || context.hasPermissions()) {
			ItemGroupEvents.MODIFY_ENTRIES_ALL.invoker().modifyEntries(tab, fabricEntries);
		}

		for (var it = entries.iterator(); it.hasNext(); ) {
			it.next();
			it.remove();
		}
		displayStacks.forEach(stack -> entries.put(stack, ItemGroup.StackVisibility.PARENT_TAB_ONLY));
		searchTabStacks.forEach(stack -> entries.put(stack, ItemGroup.StackVisibility.SEARCH_TAB_ONLY));
	}
}
