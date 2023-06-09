package net.fabricmc.fabric.impl.client.model;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;

@Mod("fabric_models_v0")
public class ModelsImpl {

    public ModelsImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(((ModelLoadingRegistryImpl) ModelLoadingRegistry.INSTANCE)::onRegisterAdditionalModels);
        }
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }
}
