/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.networking.client;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.payload.ResolvedPayload;
import net.fabricmc.fabric.impl.networking.payload.TypedPayload;
import net.fabricmc.fabric.impl.networking.payload.UntypedPayload;
import net.fabricmc.fabric.mixin.networking.client.accessor.ConnectScreenAccessor;
import net.fabricmc.fabric.mixin.networking.client.accessor.MinecraftClientAccessor;

public final class ClientNetworkingImpl {
	public static final GlobalReceiverRegistry<ClientLoginNetworking.LoginQueryRequestHandler> LOGIN = new GlobalReceiverRegistry<>(NetworkState.LOGIN);

	private static ClientConfigurationNetworkHandler currentConfigurationAddon;

	public static ClientLoginNetworkAddon getAddon(ClientLoginNetworkHandler handler) {
		return (ClientLoginNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static Packet<ServerCommonPacketListener> createC2SPacket(Identifier channelName, PacketByteBuf buf) {
		return new CustomPayloadC2SPacket(new UntypedPayload(channelName, buf));
	}

	public static Packet<ServerCommonPacketListener> createC2SPacket(FabricPacket packet) {
		Objects.requireNonNull(packet, "Packet cannot be null");
		Objects.requireNonNull(packet.getType(), "Packet#getType cannot return null");

		ResolvedPayload payload = new TypedPayload(packet);
		if (NetworkingImpl.FORCE_PACKET_SERIALIZATION) payload = payload.resolve(null);

		return new CustomPayloadC2SPacket(payload);
	}

	/**
	 * Due to the way logging into a integrated or remote dedicated server will differ, we need to obtain the login client connection differently.
	 */
	@Nullable
	public static ClientConnection getLoginConnection() {
		final ClientConnection connection = ((MinecraftClientAccessor) MinecraftClient.getInstance()).getConnection();

		// Check if we are connecting to an integrated server. This will set the field on MinecraftClient
		if (connection != null) {
			return connection;
		} else {
			// We are probably connecting to a remote server.
			// Check if the ConnectScreen is the currentScreen to determine that:
			if (MinecraftClient.getInstance().currentScreen instanceof ConnectScreen) {
				return ((ConnectScreenAccessor) MinecraftClient.getInstance().currentScreen).getConnection();
			}
		}

		// We are not connected to a server at all.
		return null;
	}

	@Nullable
	public static ClientConfigurationNetworkHandler getClientConfigurationAddon() {
		return currentConfigurationAddon;
	}

	public static void setClientConfigurationAddon(ClientConfigurationNetworkHandler addon) {
		assert addon == null;
		currentConfigurationAddon = addon;
	}

	public static void clientInit() {
		ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> {
			currentConfigurationAddon = null;
		});
	}
}
