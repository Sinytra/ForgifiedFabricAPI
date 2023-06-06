package net.fabricmc.fabric.impl.client.item.v1;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ItemApiClientEventHooks {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemTooltipCallback.EVENT.invoker().getTooltip(event.getItemStack(), event.getFlags(), event.getToolTip());
    }

    public static Player getClientPlayerSafely() {
        return Minecraft.getInstance().player;
    }

    private ItemApiClientEventHooks() {}
}
