package net.fabricmc.fabric.impl.itemgroup;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("fabric_item_group_api_v1")
public class ItemGroupsApiImpl {
    public ItemGroupsApiImpl() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ItemGroupEventsImpl::onCreativeModeTabBuildContents);
    }
}
