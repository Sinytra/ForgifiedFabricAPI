package net.fabricmc.fabric.impl.client.screen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;

@Mod("fabric_screen_api_v1")
public class ScreenApiImpl {

    public ScreenApiImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(ScreenEventHooks.class);
        }
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }
}
