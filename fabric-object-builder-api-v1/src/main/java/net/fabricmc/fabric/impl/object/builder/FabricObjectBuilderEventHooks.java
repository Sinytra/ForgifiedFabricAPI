package net.fabricmc.fabric.impl.object.builder;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber
public class FabricObjectBuilderEventHooks {

	@SubscribeEvent
	public static void onModifyEntityAttributes(EntityAttributeModificationEvent event) {
		FabricDefaultAttributeRegistryImpl.invokeModify();
	}
}
