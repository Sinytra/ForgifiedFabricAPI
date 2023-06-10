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

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.FutureListeners;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.impl.networking.AbstractNetworkAddon;
import net.fabricmc.fabric.impl.networking.GenericFutureListenerHolder;
import net.fabricmc.fabric.mixin.networking.client.accessor.ClientLoginNetworkHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ClientLoginNetworkAddon extends AbstractNetworkAddon<ClientLoginNetworking.LoginQueryRequestHandler> {
	private final ClientHandshakePacketListenerImpl handler;
	private final Minecraft client;
	private boolean firstResponse = true;

	public ClientLoginNetworkAddon(ClientHandshakePacketListenerImpl handler, Minecraft client) {
		super(ClientNetworkingImpl.LOGIN, "ClientLoginNetworkAddon for Client");
		this.handler = handler;
		this.client = client;

		ClientLoginConnectionEvents.INIT.invoker().onLoginStart(this.handler, this.client);
		this.receiver.startSession(this);
	}

	public boolean handlePacket(ClientboundCustomQueryPacket packet) {
		return handlePacket(packet.getTransactionId(), packet.getIdentifier(), packet.getData());
	}

	private boolean handlePacket(int queryId, ResourceLocation channelName, FriendlyByteBuf originalBuf) {
		this.logger.debug("Handling inbound login response with id {} and channel with name {}", queryId, channelName);

		if (this.firstResponse) {
			// Register global handlers
			for (Map.Entry<ResourceLocation, ClientLoginNetworking.LoginQueryRequestHandler> entry : ClientNetworkingImpl.LOGIN.getHandlers().entrySet()) {
				ClientLoginNetworking.registerReceiver(entry.getKey(), entry.getValue());
			}

			ClientLoginConnectionEvents.QUERY_START.invoker().onLoginQueryStart(this.handler, this.client);
			this.firstResponse = false;
		}

		@Nullable ClientLoginNetworking.LoginQueryRequestHandler handler = this.getHandler(channelName);

		if (handler == null) {
			return false;
		}

		FriendlyByteBuf buf = PacketByteBufs.slice(originalBuf);
		List<GenericFutureListener<? extends Future<? super Void>>> futureListeners = new ArrayList<>();

		try {
			CompletableFuture<@Nullable FriendlyByteBuf> future = handler.receive(this.client, this.handler, buf, futureListeners::add);
			future.thenAccept(result -> {
				ServerboundCustomQueryPacket packet = new ServerboundCustomQueryPacket(queryId, result);
				GenericFutureListener<? extends Future<? super Void>> listener = null;

				for (GenericFutureListener<? extends Future<? super Void>> each : futureListeners) {
					listener = FutureListeners.union(listener, each);
				}

				((ClientLoginNetworkHandlerAccessor) this.handler).getConnection().send(packet, GenericFutureListenerHolder.create(listener));
			});
		} catch (Throwable ex) {
			this.logger.error("Encountered exception while handling in channel with name \"{}\"", channelName, ex);
			throw ex;
		}

		return true;
	}

	@Override
	protected void handleRegistration(ResourceLocation channelName) {
	}

	@Override
	protected void handleUnregistration(ResourceLocation channelName) {
	}

	@Override
	protected void invokeDisconnectEvent() {
		ClientLoginConnectionEvents.DISCONNECT.invoker().onLoginDisconnect(this.handler, this.client);
		this.receiver.endSession(this);
	}

	public void handlePlayTransition() {
		this.receiver.endSession(this);
	}

	@Override
	protected boolean isReservedChannel(ResourceLocation channelName) {
		return false;
	}
}
