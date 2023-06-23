package net.fabricmc.fabric.impl.blockrenderlayer;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class BlockRenderLayerClientInit {
    
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockRenderLayerMapImpl.initialize());
    }
    
    private BlockRenderLayerClientInit() {}
}
