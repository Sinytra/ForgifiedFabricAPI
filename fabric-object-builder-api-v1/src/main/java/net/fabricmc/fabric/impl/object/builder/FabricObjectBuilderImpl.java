package net.fabricmc.fabric.impl.object.builder;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import org.sinytra.fabric.object_builder_api.generated.GeneratedEntryPoint;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mod(GeneratedEntryPoint.MOD_ID)
public class FabricObjectBuilderImpl {

	public FabricObjectBuilderImpl(IEventBus bus) {
		bus.addListener(FabricObjectBuilderImpl::onCommonSetup);
		bus.addListener(FabricObjectBuilderImpl::onModifyEntityAttributes);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
			((FabricBlockEntityTypeImpl) type).modifyValidBlocks();
		}
	}

	private static void onModifyEntityAttributes(EntityAttributeModificationEvent event) {
		FabricDefaultAttributeRegistryImpl.invokeModify();
	}
}
