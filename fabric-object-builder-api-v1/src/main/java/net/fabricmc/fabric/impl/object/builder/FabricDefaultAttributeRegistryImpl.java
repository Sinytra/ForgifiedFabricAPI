package net.fabricmc.fabric.impl.object.builder;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public final class FabricDefaultAttributeRegistryImpl {
    private static final Map<EntityType<? extends LivingEntity>, AttributeSupplier> SUPPLIERS = new HashMap<>();

    public static AttributeSupplier register(EntityType<? extends LivingEntity> entityType, AttributeSupplier supplier) {
        return SUPPLIERS.put(entityType, supplier);
    }

    @SubscribeEvent
    public static void onAttributesCreate(EntityAttributeCreationEvent event) {
        SUPPLIERS.forEach(event::put);
    }

    private FabricDefaultAttributeRegistryImpl() {}
}
