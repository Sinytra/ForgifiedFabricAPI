package net.fabricmc.fabric.impl.client.item;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

@EventBusSubscriber
public class ClientItemEventHooks {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemTooltipCallback.EVENT.invoker().getTooltip(event.getItemStack(), event.getContext(), event.getFlags(), event.getToolTip());
    }
}
