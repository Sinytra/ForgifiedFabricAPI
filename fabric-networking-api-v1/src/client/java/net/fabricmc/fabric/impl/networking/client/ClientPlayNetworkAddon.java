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

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.GenericFutureListenerHolder;
import net.fabricmc.fabric.impl.networking.payload.ResolvedPayload;

public final class ClientPlayNetworkAddon implements PacketSender {
	private final ClientPlayNetworkHandler handler;

	public ClientPlayNetworkAddon(ClientPlayNetworkHandler handler) {
		this.handler = handler;
	}

	@Override
	public Packet<?> createPacket(Identifier channelName, PacketByteBuf buf) {
		return ClientPlayNetworking.createC2SPacket(channelName, buf);
	}

	@Override
	public Packet<?> createPacket(FabricPacket packet) {
		return ClientPlayNetworking.createC2SPacket(packet);
	}

	@Override
	public void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback) {
		sendPacket(packet, GenericFutureListenerHolder.create(callback));
	}

	@Override
	public void sendPacket(Packet<?> packet, PacketCallbacks callback) {
		Objects.requireNonNull(packet, "Packet cannot be null");

		this.handler.connection.send(packet, callback);
	}

	public interface Handler {
		void receive(MinecraftClient client, ClientPlayNetworkHandler handler, ResolvedPayload payload, PacketSender responseSender);
	}
}
