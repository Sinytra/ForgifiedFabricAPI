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

package net.fabricmc.fabric.impl.networking.server;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.netty.channel.ChannelFutureListener;
import net.neoforged.neoforge.network.connection.ConnectionType;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.ClientboundConfigurationChannelEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;

public final class ServerConfigurationNetworkAddon extends AbstractChanneledNetworkAddon<ServerConfigurationNetworking.ConfigurationPacketHandler<?>> {
	private final ServerConfigurationPacketListenerImpl listener;
	private final MinecraftServer server;
	private final ServerConfigurationNetworking.Context context;
	private RegisterState registerState = RegisterState.NOT_SENT;
	private boolean isReconfiguring = false;

	public ServerConfigurationNetworkAddon(ServerConfigurationPacketListenerImpl listener, MinecraftServer server) {
		super(ServerNetworkingImpl.CONFIGURATION, listener.getConnection(), "ServerConfigurationNetworkAddon for " + listener.getOwner().name());
		this.listener = listener;
		this.server = server;
		this.context = new ContextImpl(server, listener, this);

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, ConnectionProtocol.CONFIGURATION);
	}

	@Override
	public boolean handle(CustomPacketPayload payload) {
		return !(payload instanceof BrandPayload) && super.handle(payload);
	}

	@Override
	protected boolean isOnReceiveThread() {
		// Configuration packets are handled on the network thread.
		return true;
	}

	@Override
	protected void invokeInitEvent() {
	}

	public void preConfiguration() {
		if (listener.getConnectionType() == ConnectionType.NEOFORGE) {
			registerState = RegisterState.RECEIVED;
		}

		ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.invoker().onSendConfiguration(listener, server);
	}

	public void configuration() {
		ServerConfigurationConnectionEvents.CONFIGURE.invoker().onSendConfiguration(listener, server);
	}

	@Override
	protected void receive(ServerConfigurationNetworking.ConfigurationPacketHandler<?> listener, CustomPacketPayload payload) {
		((ServerConfigurationNetworking.ConfigurationPacketHandler) listener).receive(payload, this.context);
	}

	// impl details

	@Override
	protected void schedule(Runnable task) {
		this.server.execute(task);
	}

	@Override
	public Packet<?> createPacket(CustomPacketPayload packet) {
		return ServerConfigurationNetworking.createClientboundPacket(packet);
	}

	@Override
	protected void invokeRegisterEvent(List<Identifier> ids) {
		ClientboundConfigurationChannelEvents.REGISTER.invoker().onChannelRegister(this.listener, this, this.server, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<Identifier> ids) {
		ClientboundConfigurationChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.listener, this, this.server, ids);
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
		// If we can already send packets, immediately send the register packet for this channel
		if (this.registerState != RegisterState.NOT_SENT) {
			CustomPacketPayload registrationPayload = this.createRegistrationPayload(RegistrationPayload.REGISTER, Collections.singleton(channelName));

			if (registrationPayload != null) {
				this.sendPacket(registrationPayload);
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		// If we can already send packets, immediately send the unregister packet for this channel
		if (this.registerState != RegisterState.NOT_SENT) {
			CustomPacketPayload registrationPayload = this.createRegistrationPayload(RegistrationPayload.UNREGISTER, Collections.singleton(channelName));

			if (registrationPayload != null) {
				this.sendPacket(registrationPayload);
			}
		}
	}

	@Override
	protected void invokeDisconnectEvent() {
		ServerConfigurationConnectionEvents.DISCONNECT.invoker().onConfigureDisconnect(listener, server);
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return NetworkingImpl.isReservedCommonChannel(channelName);
	}

	@Override
	public void sendPacket(Packet<?> packet, ChannelFutureListener callback) {
		listener.send(packet, callback);
	}

	public boolean isReconfiguring() {
		return isReconfiguring;
	}

	public void setReconfiguring() {
		isReconfiguring = true;
	}

	private enum RegisterState {
		NOT_SENT,
		SENT,
		RECEIVED,
		NOT_RECEIVED
	}

	public ChannelInfoHolder getChannelInfoHolder() {
		return (ChannelInfoHolder) listener.getConnection();
	}

	private record ContextImpl(MinecraftServer server, ServerConfigurationPacketListenerImpl packetListener, PacketSender responseSender) implements ServerConfigurationNetworking.Context {
		private ContextImpl {
			Objects.requireNonNull(server, "server");
			Objects.requireNonNull(packetListener, "packetListener");
			Objects.requireNonNull(responseSender, "responseSender");
		}
	}
}
