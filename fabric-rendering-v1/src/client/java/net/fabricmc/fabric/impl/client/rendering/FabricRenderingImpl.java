package net.fabricmc.fabric.impl.client.rendering;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import org.sinytra.fabric.rendering.generated.GeneratedEntryPoint;

import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;

@Mod(GeneratedEntryPoint.MOD_ID)
public class FabricRenderingImpl {
	
	public FabricRenderingImpl(IEventBus bus) {
		bus.addListener(FabricRenderingImpl::registerPictureInPictureRenderers);
		bus.addListener(HudElementRegistryImpl::register);
	}
	
	private static void registerPictureInPictureRenderers(RegisterPictureInPictureRenderersEvent event) {
		PictureInPictureRendererRegistryImpl.apply(event);
	}
}
