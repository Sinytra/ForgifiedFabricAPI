package net.fabricmc.fabric.test.resource.loader;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(ResourceLoaderTestImpl.MODID)
public class ResourceLoaderTestImpl {
    public static final String MODID = "fabric_resource_loader_v0_testmod";

    public ResourceLoaderTestImpl() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        if (FMLLoader.getDist() == Dist.DEDICATED_SERVER) {
            MinecraftForge.EVENT_BUS.addListener(LanguageTestMod::onInitializeServer);
        }
        BuiltinResourcePackTestMod.onInitialize();
        ResourceReloadListenerTestMod.onInitialize();
        VanillaBuiltinResourcePackInjectionTestMod.onInitialize(bus);
    }
}
