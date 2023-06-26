package net.fabricmc.fabric.impl.object.builder;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("fabric_object_builder_api_v1")
public class ObjectBuilderApiImpl {

    public ObjectBuilderApiImpl() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(FabricDefaultAttributeRegistryImpl.class);

        MinecraftForge.EVENT_BUS.register(TradeOfferInternals.class);
    }
}
