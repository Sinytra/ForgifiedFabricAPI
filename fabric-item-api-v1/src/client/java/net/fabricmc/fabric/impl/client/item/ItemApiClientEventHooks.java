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
