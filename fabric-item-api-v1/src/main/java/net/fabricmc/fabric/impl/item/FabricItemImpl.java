package net.fabricmc.fabric.impl.item;

import net.fabricmc.fabric.impl.client.item.v1.ItemApiClientEventHooks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_item_api_v1")
public class FabricItemImpl {
    
    public FabricItemImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(ItemApiClientEventHooks.class);
        }
        MinecraftForge.EVENT_BUS.register(ItemApiEventHooks.class);
    }
}
