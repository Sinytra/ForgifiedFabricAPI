package net.fabricmc.fabric.impl.networking;

import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonPacketListenerImplAccessor;

import net.minecraft.server.MinecraftServer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import org.sinytra.fabric.networking_api.generated.GeneratedEntryPoint;

import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;

@Mod(GeneratedEntryPoint.MOD_ID)
public class NetworkingEventHooks {

	public NetworkingEventHooks(IEventBus bus) {
		bus.addListener(NetworkingEventHooks::onConfiguration);
	}

	private static void onConfiguration(RegisterConfigurationTasksEvent event) {
		ServerConfigurationNetworkAddon addon = (ServerConfigurationNetworkAddon) ((PacketListenerExtensions) event.getListener()).getAddon();
		addon.configuration();
	}
}
