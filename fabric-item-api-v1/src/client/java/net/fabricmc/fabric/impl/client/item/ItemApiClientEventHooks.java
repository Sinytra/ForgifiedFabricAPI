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

package net.fabricmc.fabric.impl.client.item;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public final class ItemApiClientEventHooks {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemTooltipCallback.EVENT.invoker().getTooltip(event.getItemStack(), event.getFlags(), event.getToolTip());
    }

    public static PlayerEntity getClientPlayerSafely() {
		return MinecraftClient.getInstance().player;
    }

    private ItemApiClientEventHooks() {}
}
