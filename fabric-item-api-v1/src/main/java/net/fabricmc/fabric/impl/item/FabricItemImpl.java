package net.fabricmc.fabric.impl.item;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;
import net.fabricmc.fabric.impl.client.item.ItemApiClientEventHooks;

@Mod("fabric_item_api_v1")
public class FabricItemImpl {

	public FabricItemImpl() {
		if (FMLLoader.getDist() == Dist.CLIENT) {
			MinecraftForge.EVENT_BUS.register(ItemApiClientEventHooks.class);
		}
		MinecraftForge.EVENT_BUS.addListener(FabricItemImpl::modifyItemAttributeModifiers);
	}

	private static void modifyItemAttributeModifiers(ItemAttributeModifierEvent event) {
		ModifyItemAttributeModifiersCallback.EVENT.invoker().modifyAttributeModifiers(event.getItemStack(), event.getSlotType(), event.getModifiers());
	}
}
