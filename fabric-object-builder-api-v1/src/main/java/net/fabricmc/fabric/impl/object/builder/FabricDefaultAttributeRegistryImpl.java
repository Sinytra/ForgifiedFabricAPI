package net.fabricmc.fabric.impl.object.builder;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;

public final class FabricDefaultAttributeRegistryImpl {
    private static final Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> SUPPLIERS = new HashMap<>();

    public static DefaultAttributeContainer register(EntityType<? extends LivingEntity> entityType, DefaultAttributeContainer supplier) {
        return SUPPLIERS.put(entityType, supplier);
    }

    @SubscribeEvent
    public static void onAttributesCreate(EntityAttributeCreationEvent event) {
        SUPPLIERS.forEach(event::put);
    }

    private FabricDefaultAttributeRegistryImpl() {}
}
