package net.fabricmc.fabric.impl.client.rendering;

import net.minecraft.resources.ResourceLocation;
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

    /**
     * Rewrites the input string containing an identifier
     * with the namespace of the id in the front instead of in the middle.
     *
     * <p>Example: {@code shaders/core/my_mod:xyz} -> {@code my_mod:shaders/core/xyz}
     *
     * @param input       the raw input string
     * @param containedId the ID contained within the input string
     * @return the corrected full ID string
     */
    public static String rewriteAsId(String input, String containedId) {
        ResourceLocation contained = new ResourceLocation(containedId);
        return contained.getNamespace() + ResourceLocation.NAMESPACE_SEPARATOR + input.replace(containedId, contained.getPath());
    }
}
