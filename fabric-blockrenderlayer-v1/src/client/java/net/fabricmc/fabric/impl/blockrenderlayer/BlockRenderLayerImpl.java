package net.fabricmc.fabric.impl.blockrenderlayer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_blockrenderlayer_v1")
public class BlockRenderLayerImpl {
    public BlockRenderLayerImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(BlockRenderLayerClientInit::onClientSetup);
        }
    }
}
