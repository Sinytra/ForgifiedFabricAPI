package net.fabricmc.fabric.impl.resource.loader;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_resource_loader_v0")
public class ResourceLoaderImpl {

    public ResourceLoaderImpl() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        if (FMLLoader.getDist() == Dist.CLIENT) {
            bus.addListener(ResourceManagerHelperImpl::onClientResourcesReload);
        }
        bus.addListener(ResourceLoaderImpl::addPackFinders);
        MinecraftForge.EVENT_BUS.addListener(ResourceManagerHelperImpl::onServerDataReload);
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        event.addRepositorySource(new ModResourcePackCreator(event.getPackType()));
    }
}
