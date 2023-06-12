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

package net.fabricmc.fabric.api.client.networking.v1;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Offers access to login stage client-side networking functionalities.
 *
 * <p>The Minecraft login protocol only allows the client to respond to a server's request, but not initiate one of its own.
 *
 * @see ClientPlayNetworking
 * @see ServerLoginNetworking
 */
public final class ClientLoginNetworking {
	/**
	 * Registers a handler to a query request channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterGlobalReceiver(ResourceLocation)} to unregister the existing handler.
	 *
	 * @param channelName the id of the channel
	 * @param queryHandler the handler
	 * @return false if a handler is already registered to the channel
	 * @see ClientLoginNetworking#unregisterGlobalReceiver(ResourceLocation)
	 * @see ClientLoginNetworking#registerReceiver(ResourceLocation, LoginQueryRequestHandler)
	 */
	public static boolean registerGlobalReceiver(ResourceLocation channelName, LoginQueryRequestHandler queryHandler) {
		return ClientNetworkingImpl.LOGIN.registerGlobalReceiver(channelName, queryHandler);
	}

	/**
	 * Removes the handler of a query request channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The {@code channel} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the id of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @see ClientLoginNetworking#registerGlobalReceiver(ResourceLocation, LoginQueryRequestHandler)
	 * @see ClientLoginNetworking#unregisterReceiver(ResourceLocation)
	 */
	@Nullable
	public static ClientLoginNetworking.LoginQueryRequestHandler unregisterGlobalReceiver(ResourceLocation channelName) {
		return ClientNetworkingImpl.LOGIN.unregisterGlobalReceiver(channelName);
	}

	/**
	 * Gets all query request channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for.
	 */
	public static Set<ResourceLocation> getGlobalReceivers() {
		return ClientNetworkingImpl.LOGIN.getChannels();
	}

	/**
	 * Registers a handler to a query request channel.
	 *
	 * <p>If a handler is already registered to the {@code channelName}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ResourceLocation)} to unregister the existing handler.
	 *
	 * @param channelName the id of the channel
	 * @param queryHandler the handler
	 * @return false if a handler is already registered to the channel name
	 * @throws IllegalStateException if the client is not logging in
	 */
	public static boolean registerReceiver(ResourceLocation channelName, LoginQueryRequestHandler queryHandler) throws IllegalStateException {
		final Connection connection = ClientNetworkingImpl.getLoginConnection();

		if (connection != null) {
			final PacketListener packetListener = connection.getPacketListener();

			if (packetListener instanceof ClientHandshakePacketListenerImpl clientHandshakePacketListener) {
				return ClientNetworkingImpl.getAddon(clientHandshakePacketListener).registerChannel(channelName, queryHandler);
			}
		}

		throw new IllegalStateException("Cannot register receiver while client is not logging in!");
	}

	/**
	 * Removes the handler of a query request channel.
	 *
	 * <p>The {@code channelName} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the id of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel name
	 * @throws IllegalStateException if the client is not logging in
	 */
	@Nullable
	public static LoginQueryRequestHandler unregisterReceiver(ResourceLocation channelName) throws IllegalStateException {
		final Connection connection = ClientNetworkingImpl.getLoginConnection();

		if (connection != null) {
			final PacketListener packetListener = connection.getPacketListener();

			if (packetListener instanceof ClientHandshakePacketListenerImpl handshakePacketListener) {
				return ClientNetworkingImpl.getAddon(handshakePacketListener).unregisterChannel(channelName);
			}
		}

		throw new IllegalStateException("Cannot unregister receiver while client is not logging in!");
	}

	private ClientLoginNetworking() {
	}

	@FunctionalInterface
	public interface LoginQueryRequestHandler {
		/**
		 * Handles an incoming query request from a server.
		 *
		 * <p>This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain net.minecraft.util.thread.BlockableEventLoop#submit(Runnable) scheduled} using the provided Minecraft client instance.
		 *
		 * <p>The return value of this method is a completable future that may be used to delay the login process to the server until a task {@link CompletableFuture#isDone() is done}.
		 * The future should complete in reasonably time to prevent disconnection by the server.
		 * If your request processes instantly, you may use {@link CompletableFuture#completedFuture(Object)} to wrap your response for immediate sending.
		 *
		 * @param client the client
		 * @param handler the network handler that received this packet
		 * @param buf the payload of the packet
		 * @param listenerAdder listeners to be called when the response packet is sent to the server
		 * @return a completable future which contains the payload to respond to the server with.
		 * If the future contains {@code null}, then the server will be notified that the client did not understand the query.
		 */
		CompletableFuture<@Nullable FriendlyByteBuf> receive(Minecraft client, ClientHandshakePacketListenerImpl handler, FriendlyByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder);
	}
}