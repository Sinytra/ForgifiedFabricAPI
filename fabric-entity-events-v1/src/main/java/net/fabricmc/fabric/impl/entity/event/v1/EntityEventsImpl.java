package net.fabricmc.fabric.impl.entity.event.v1;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_entity_events_v1")
public class EntityEventsImpl {

    public EntityEventsImpl() {
        MinecraftForge.EVENT_BUS.register(EntityEventHooks.class);
    }
}
