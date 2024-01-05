package net.fabricmc.fabric.impl.networking.client;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.C2SConfigurationChannelEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.neo.DelayedNetworkRegister;
import net.fabricmc.fabric.impl.networking.neo.ServerNeoNetworking;

@Mod.EventBusSubscriber
public class ClientNeoNetworking {
	public static final DelayedNetworkRegister<ClientConfigurationNetworkHandler, ClientConfigurationNetworkAddon.Handler, MinecraftClient> CONFIGURATION =
			new DelayedNetworkRegister<>(
					ServerNeoNetworking::unwrapHandler, handler -> MinecraftClient.getInstance(),
					(handler, client, sender, internalHandler, payload, context) -> internalHandler.receive(client, handler, payload, sender)
			);
	public static final DelayedNetworkRegister<ClientPlayNetworkHandler, ClientPlayNetworkAddon.Handler, MinecraftClient> PLAY =
			new DelayedNetworkRegister<>(
					ServerNeoNetworking::unwrapHandler, handler -> MinecraftClient.getInstance(),
					(handler, client, sender, internalHandler, payload, context) -> internalHandler.receive(client, handler, payload, sender)
			);

	@SubscribeEvent
	public static void enterRegistrationPhase(OnGameConfigurationEvent event) {
		ClientConfigurationNetworkHandler handler = (ClientConfigurationNetworkHandler) event.getListener();
		PacketSender sender = (PacketSender) ((NetworkHandlerExtensions) handler).getAddon();
		MinecraftClient client = MinecraftClient.getInstance();
		List<Identifier> ids = new ArrayList<>(); // TODO
		C2SConfigurationChannelEvents.REGISTER.invoker().onChannelRegister(handler, sender, client, ids);

		// TODO Unregister
	}

	// TODO C2SConfigurationChannelEvents
	// TODO C2SPlayChannelEvents
	// TODO ClientConfigurationConnectionEvents

	@SubscribeEvent
	public static void register(RegisterPayloadHandlerEvent event) {
		CONFIGURATION.apply(event);
	}
}
