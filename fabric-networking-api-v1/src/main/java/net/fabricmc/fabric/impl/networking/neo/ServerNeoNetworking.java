package net.fabricmc.fabric.impl.networking.neo;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPacketHandler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.S2CConfigurationChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import net.fabricmc.fabric.mixin.networking.accessor.ServerPacketHandlerAccessor;

@Mod.EventBusSubscriber
public class ServerNeoNetworking {
	public static final DelayedNetworkRegister<ServerConfigurationNetworkHandler, ServerConfigurationNetworkAddon.Handler, MinecraftServer> CONFIGURATION =
			new DelayedNetworkRegister<>(
					ServerNeoNetworking::unwrapHandler, ServerConfigurationNetworking::getServer,
					(handler, server, sender, internalHandler, payload, context) -> internalHandler.receive(server, handler, payload, sender)
			);
	public static final DelayedNetworkRegister<ServerPlayNetworkHandler, ServerPlayNetworkAddon.Handler, MinecraftServer> PLAY =
			new DelayedNetworkRegister<>(
					ServerNeoNetworking::unwrapHandler, ServerPlayNetworking::getServer,
					(handler, server, sender, internalHandler, payload, context) -> internalHandler.receive(server, handler.player, handler, payload, sender)
			);

	@SubscribeEvent
	public static void enterRegistrationPhase(OnGameConfigurationEvent event) {
		ServerConfigurationNetworkHandler handler = (ServerConfigurationNetworkHandler) event.getListener();
		PacketSender sender = (PacketSender) ((NetworkHandlerExtensions) handler).getAddon();
		MinecraftServer server = ServerConfigurationNetworking.getServer(handler);
		List<Identifier> ids = new ArrayList<>(); // TODO
		S2CConfigurationChannelEvents.REGISTER.invoker().onChannelRegister(handler, sender, server, ids);

		// TODO Unregister
	}

	// TODO S2CPlayChannelEvents
	// TODO ServerConfigurationConnectionEvents
	// TODO ServerLoginConnectionEvents
	// TODO ServerLoginNetworking
	// TODO ServerPlayConnectionEvents

	@SubscribeEvent
	public static void register(RegisterPayloadHandlerEvent event) {
		CONFIGURATION.apply(event);
	}

	public static <T> T unwrapHandler(IPacketHandler handler) {
		return (T) ((ServerPacketHandlerAccessor) handler).getListener();
	}
}
