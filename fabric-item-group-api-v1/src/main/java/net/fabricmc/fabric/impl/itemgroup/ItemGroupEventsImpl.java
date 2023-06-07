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

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.IdentifiableItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.MutableHashedLinkedMap;
import net.minecraftforge.event.CreativeModeTabEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemGroupEventsImpl {
    private static final Map<ResourceLocation, Event<ItemGroupEvents.ModifyEntries>> IDENTIFIER_EVENT_MAP = new HashMap<>();

    public static Event<ItemGroupEvents.ModifyEntries> getOrCreateModifyEntriesEvent(ResourceLocation identifier) {
        return IDENTIFIER_EVENT_MAP.computeIfAbsent(identifier, g -> createModifyEvent());
    }

    @Nullable
    public static Event<ItemGroupEvents.ModifyEntries> getModifyEntriesEvent(ResourceLocation identifier) {
        return IDENTIFIER_EVENT_MAP.get(identifier);
    }

    private static Event<ItemGroupEvents.ModifyEntries> createModifyEvent() {
        return EventFactory.createArrayBacked(ItemGroupEvents.ModifyEntries.class, callbacks -> entries -> {
            for (ItemGroupEvents.ModifyEntries callback : callbacks) {
                callback.modifyEntries(entries);
            }
        });
    }

    static void onCreativeModeTabBuildContents(CreativeModeTabEvent.BuildContents event) {
        CreativeModeTab tab = event.getTab();
        CreativeModeTab.ItemDisplayParameters context = event.getParameters();

        List<ItemStack> displayStacks = new ArrayList<>();
        List<ItemStack> searchTabStacks = new ArrayList<>();
        MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries = event.getEntries();
        entries.forEach(entry -> {
            ItemStack stack = entry.getKey();
            CreativeModeTab.TabVisibility visibility = entry.getValue();
            if (visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS || visibility == CreativeModeTab.TabVisibility.PARENT_TAB_ONLY) {
                displayStacks.add(stack);
            }
            if (visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS || visibility == CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY) {
                searchTabStacks.add(stack);
            }
        });

        FabricItemGroupEntries fabricEntries = new FabricItemGroupEntries(context, displayStacks, searchTabStacks);
        
        Event<ItemGroupEvents.ModifyEntries> modifyEntriesEvent = getModifyEntriesEvent(((IdentifiableItemGroup) tab).getId());
        if (modifyEntriesEvent != null) {
            modifyEntriesEvent.invoker().modifyEntries(fabricEntries);
        }

        // Now trigger the global event
        if (tab != CreativeModeTabs.OP_BLOCKS || context.hasPermissions()) {
            ItemGroupEvents.MODIFY_ENTRIES_ALL.invoker().modifyEntries(tab, fabricEntries);
        }

        for (var it = entries.iterator(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
        displayStacks.forEach(stack -> entries.put(stack, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY));
        searchTabStacks.forEach(stack -> entries.put(stack, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY));
    }
}
