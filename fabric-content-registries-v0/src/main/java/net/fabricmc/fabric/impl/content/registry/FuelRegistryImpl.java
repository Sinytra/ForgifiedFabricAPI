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

package net.fabricmc.fabric.impl.content.registry;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Clamp values to 32767 (+ add hook for mods which extend the limit to disable the check?)
public final class FuelRegistryImpl implements FuelRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(FuelRegistryImpl.class);
    private final Object2IntMap<ItemLike> itemCookTimes = new Object2IntLinkedOpenHashMap<>();
    private final Object2IntMap<TagKey<Item>> tagCookTimes = new Object2IntLinkedOpenHashMap<>();

    public FuelRegistryImpl() {
        MinecraftForge.EVENT_BUS.addListener(this::onFurnaceFueldBurnTime);
    }

    private void onFurnaceFueldBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();

        for (ItemLike item : itemCookTimes.keySet()) {
            if (stack.is(item.asItem())) {
                int time = itemCookTimes.getInt(item);
                if (time > 0) {
                    event.setBurnTime(time);
                    return;
                }
            }
        }

        for (TagKey<Item> tag : tagCookTimes.keySet()) {
            if (stack.is(tag)) {
                int time = tagCookTimes.getInt(tag);
                if (time > 0) {
                    event.setBurnTime(time);
                    return;
                }
            }
        }
    }

    @Override
    public Integer get(ItemLike item) {
        return ForgeHooks.getBurnTime(new ItemStack(item), null);
    }

    @Override
    public void add(ItemLike item, Integer cookTime) {
        if (cookTime > 32767) {
            LOGGER.warn("Tried to register an overly high cookTime: " + cookTime + " > 32767! (" + item + ")");
        }

        itemCookTimes.put(item, cookTime.intValue());
    }

    @Override
    public void add(TagKey<Item> tag, Integer cookTime) {
        if (cookTime > 32767) {
            LOGGER.warn("Tried to register an overly high cookTime: " + cookTime + " > 32767! (" + getTagName(tag) + ")");
        }

        tagCookTimes.put(tag, cookTime.intValue());
    }

    @Override
    public void remove(ItemLike item) {
        add(item, 0);
    }

    @Override
    public void remove(TagKey<Item> tag) {
        add(tag, 0);
    }

    @Override
    public void clear(ItemLike item) {
        itemCookTimes.removeInt(item);
    }

    @Override
    public void clear(TagKey<Item> tag) {
        tagCookTimes.removeInt(tag);
    }

    private static String getTagName(TagKey<?> tag) {
        return tag.location().toString();
    }
}
