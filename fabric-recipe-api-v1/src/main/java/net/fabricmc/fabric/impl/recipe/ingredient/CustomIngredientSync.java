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

package net.fabricmc.fabric.impl.recipe.ingredient;

import io.netty.channel.ChannelHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.mixin.recipe.ingredient.PacketEncoderMixin;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketEncoder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * To reasonably support server-side only custom ingredients, we only send custom ingredients to clients that support them.
 * If a specific client doesn't support a custom ingredient, we send the matching stacks as a regular ingredient.
 * This is fine since all recipe computation happens server-side anyway.
 *
 * <p><ul>
 *     <li>Each client sends a packet with the set of custom ingredients it supports.</li>
 *     <li>We store that set inside the {@link PacketEncoder} using {@link PacketEncoderMixin}.</li>
 *     <li>When serializing a custom ingredient, we get access to the current {@link PacketEncoder},
 *     and based on that we decide whether to send the custom ingredient, or a vanilla ingredient with the matching stacks.</li>
 * </ul>
 */
public class CustomIngredientSync {
	public static final ResourceLocation PACKET_ID = new ResourceLocation("fabric", "custom_ingredient_sync");
	public static final int PROTOCOL_VERSION_1 = 1;
	public static final ThreadLocal<Set<ResourceLocation>> CURRENT_SUPPORTED_INGREDIENTS = new ThreadLocal<>();

	@Nullable
	public static FriendlyByteBuf createResponsePacket(int serverProtocolVersion) {
		if (serverProtocolVersion < PROTOCOL_VERSION_1) {
			// Not supposed to happen - notify the server that we didn't understand the query.
			return null;
		}

		// Always send protocol 1 - the server should support it even if it supports more recent protocols.
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(PROTOCOL_VERSION_1);
		buf.writeCollection(CustomIngredientImpl.REGISTERED_SERIALIZERS.keySet(), FriendlyByteBuf::writeResourceLocation);
		return buf;
	}

	public static Set<ResourceLocation> decodeResponsePacket(FriendlyByteBuf buf) {
		int protocolVersion = buf.readVarInt();

		switch (protocolVersion) {
		case PROTOCOL_VERSION_1 -> {
			Set<ResourceLocation> identifiers = buf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation);
			// Remove unknown keys to save memory
			identifiers.removeIf(id -> !CustomIngredientImpl.REGISTERED_SERIALIZERS.containsKey(id));
			return identifiers;
		}
		default -> {
			throw new IllegalArgumentException("Unknown ingredient sync protocol version: " + protocolVersion);
		}
		}
	}

	public static void onInitialize() {
		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			// Send packet with 1 so the client can send us back the list of supported tags.
			// 1 is sent in case we need a different protocol later for some reason.
			FriendlyByteBuf buf = PacketByteBufs.create();
			buf.writeVarInt(PROTOCOL_VERSION_1); // max supported server protocol version
			sender.sendPacket(PACKET_ID, buf);
		});
		ServerLoginNetworking.registerGlobalReceiver(PACKET_ID, (server, handler, understood, buf, synchronizer, responseSender) -> {
			if (!understood) {
				// Skip if the client didn't understand the query.
				return;
			}

			Set<ResourceLocation> supportedCustomIngredients = decodeResponsePacket(buf);
			ChannelHandler packetEncoder = handler.connection.channel().pipeline().get("encoder");

			if (packetEncoder != null) { // Null in singleplayer
				((SupportedIngredientsPacketEncoder) packetEncoder).fabric_setSupportedCustomIngredients(supportedCustomIngredients);
			}
		});
	}
}