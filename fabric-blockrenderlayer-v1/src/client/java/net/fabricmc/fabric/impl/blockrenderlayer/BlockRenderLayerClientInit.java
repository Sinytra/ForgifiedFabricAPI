package net.fabricmc.fabric.impl.blockrenderlayer;

import net.minecraft.client.render.RenderLayers;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class BlockRenderLayerClientInit {
    
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockRenderLayerMapImpl.initialize(RenderLayers::setRenderLayer, RenderLayers::setRenderLayer));
    }
    
    private BlockRenderLayerClientInit() {}
}
