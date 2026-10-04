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

package net.fabricmc.fabric.impl.networking;

import java.util.Set;

import io.netty.util.AttributeKey;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class NetworkingImpl {
	public static final String MOD_ID = "fabric-networking-api-v1";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Id of packet used to register supported channels.
	 */
	public static final Identifier REGISTER_CHANNEL = Identifier.withDefaultNamespace("register");

	/**
	 * Id of packet used to unregister supported channels.
	 */
	public static final Identifier UNREGISTER_CHANNEL = Identifier.withDefaultNamespace("unregister");

	public static final AttributeKey<Set<Identifier>> SENDABLE_CHANNELS = AttributeKey.valueOf("fabric:channels");

	public static boolean isReservedCommonChannel(Identifier channelName) {
		return channelName.equals(REGISTER_CHANNEL) || channelName.equals(UNREGISTER_CHANNEL);
	}

	public static CustomPacketPayload.@Nullable TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> getCodec(Identifier id, ConnectionProtocol protocol, PacketFlow flow) {
		if (flow == PacketFlow.CLIENTBOUND) {
			if (protocol == ConnectionProtocol.PLAY) {
				return PayloadTypeRegistryImpl.CLIENTBOUND_PLAY.get(id);
			}
			if (protocol == ConnectionProtocol.CONFIGURATION) {
				return PayloadTypeRegistryImpl.CLIENTBOUND_CONFIGURATION.get(id);
			}
		}
		if (flow == PacketFlow.SERVERBOUND) {
			if (protocol == ConnectionProtocol.PLAY) {
				return PayloadTypeRegistryImpl.SERVERBOUND_PLAY.get(id);
			}
			if (protocol == ConnectionProtocol.CONFIGURATION) {
				return PayloadTypeRegistryImpl.SERVERBOUND_CONFIGURATION.get(id);
			}
		}
		return null;
	}
}
