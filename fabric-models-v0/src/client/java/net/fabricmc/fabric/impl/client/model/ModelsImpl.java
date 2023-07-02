package net.fabricmc.fabric.impl.client.model;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

@Mod("fabric_models_v0")
public class ModelsImpl {

    public ModelsImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(((ModelLoadingRegistryImpl) ModelLoadingRegistry.INSTANCE)::onRegisterAdditionalModels);
        }
    }
}
