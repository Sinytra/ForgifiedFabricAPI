package net.fabricmc.fabric.impl.networking;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import org.sinytra.fabric.networking_api.generated.GeneratedEntryPoint;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;

@Mod(GeneratedEntryPoint.MOD_ID)
public class NetworkingEventHooks {

	public NetworkingEventHooks(IEventBus bus) {
		bus.addListener(NetworkingEventHooks::onConfiguration);
		NeoForge.EVENT_BUS.addListener(NetworkingEventHooks::onStartTrackingEntity);
		NeoForge.EVENT_BUS.addListener(NetworkingEventHooks::onStopTrackingEntity);
	}

	private static void onConfiguration(RegisterConfigurationTasksEvent event) {
		ServerConfigurationNetworkAddon addon = (ServerConfigurationNetworkAddon) ((PacketListenerExtensions) event.getListener()).getAddon();
		addon.configuration();
	}
	
	private static void onStartTrackingEntity(PlayerEvent.StartTracking event) {
		EntityTrackingEvents.START_TRACKING.invoker().onStartTracking(event.getTarget(), (ServerPlayer) event.getEntity());
	}
	
	private static void onStopTrackingEntity(PlayerEvent.StopTracking event) {
		EntityTrackingEvents.STOP_TRACKING.invoker().onStopTracking(event.getTarget(), (ServerPlayer) event.getEntity());
	}
}
