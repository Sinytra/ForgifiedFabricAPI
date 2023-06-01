package net.fabricmc.fabric.impl.event.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LivingEntityEventHooks {
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        ServerEntityEvents.EQUIPMENT_CHANGE.invoker().onChange(event.getEntity(), event.getSlot(), event.getFrom(), event.getTo());
    }
    
    private LivingEntityEventHooks() {}
}
