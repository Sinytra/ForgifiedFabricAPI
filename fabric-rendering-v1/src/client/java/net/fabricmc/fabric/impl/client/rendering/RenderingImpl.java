package net.fabricmc.fabric.impl.client.rendering;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_rendering_v1")
public class RenderingImpl {

    public RenderingImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(ClientRenderingEventHooks::onRegisterBlockColors);
            bus.addListener(ClientRenderingEventHooks::onRegisterItemColors);
            bus.addListener(ClientRenderingEventHooks::onRegisterShaders);
            bus.addListener(ClientRenderingEventHooks::registerEntityRenderers);
            bus.addListener(ClientRenderingEventHooks::registerLayerDefinitions);

            MinecraftForge.EVENT_BUS.addListener(ClientRenderingEventHooks::onPostRenderHud);
        }
    }
}
