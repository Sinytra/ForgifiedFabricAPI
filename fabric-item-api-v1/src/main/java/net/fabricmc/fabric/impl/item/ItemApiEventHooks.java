package net.fabricmc.fabric.impl.item;

import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ItemApiEventHooks {

    @SubscribeEvent
    public static void modifyItemAttributeModifiers(ItemAttributeModifierEvent event) {
        ModifyItemAttributeModifiersCallback.EVENT.invoker().modifyAttributeModifiers(event.getItemStack(), event.getSlotType(), event.getModifiers());
    }

    private ItemApiEventHooks() {}
}
