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

import java.util.Objects;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import net.minecraft.network.ClientConnection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.GenericFutureListenerHolder;
import net.fabricmc.fabric.impl.networking.payload.ResolvedPayload;

public final class ServerPlayNetworkAddon implements PacketSender {
	private final ClientConnection connection;

	public ServerPlayNetworkAddon(ClientConnection connection) {
		this.connection = connection;
	}

	@Override
	public Packet<?> createPacket(Identifier channelName, PacketByteBuf buf) {
		return ServerPlayNetworking.createS2CPacket(channelName, buf);
	}

	@Override
	public Packet<?> createPacket(FabricPacket packet) {
		return ServerPlayNetworking.createS2CPacket(packet);
	}

	@Override
	public void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback) {
		sendPacket(packet, GenericFutureListenerHolder.create(callback));
	}

	@Override
	public void sendPacket(Packet<?> packet, PacketCallbacks callback) {
		Objects.requireNonNull(packet, "Packet cannot be null");

		this.connection.send(packet, callback);
	}

	public interface Handler {
		void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, ResolvedPayload payload, PacketSender responseSender);
	}
}
