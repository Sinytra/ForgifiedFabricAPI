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

package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * A type of packet. An instance of this should be created per a {@link FabricPacket} implementation.
 * This holds the channel ID used for the packet.
 *
 * @param <T> the type of the packet
 * @see FabricPacket
 */
public final class PacketType<T extends FabricPacket> {
	private final ResourceLocation id;
	private final Function<FriendlyByteBuf, T> constructor;

	private PacketType(ResourceLocation id, Function<FriendlyByteBuf, T> constructor) {
		this.id = id;
		this.constructor = constructor;
	}

	/**
	 * Creates a new packet type.
	 * @param id the channel ID used for the packets
	 * @param constructor the reader that reads the received buffer
	 * @param <P> the type of the packet
	 * @return the newly created type
	 */
	public static <P extends FabricPacket> PacketType<P> create(ResourceLocation id, Function<FriendlyByteBuf, P> constructor) {
		return new PacketType<>(id, constructor);
	}

	/**
	 * Returns the identifier of the channel used to send the packet.
	 * @return the identifier of the associated channel.
	 */
	public ResourceLocation getId() {
		return id;
	}

	/**
	 * Reads the packet from the buffer.
	 * @param buf the buffer
	 * @return the packet
	 */
	public T read(FriendlyByteBuf buf) {
		try {
			return this.constructor.apply(buf);
		} catch (RuntimeException e) {
			throw new RuntimeException("Error while handling packet \"%s\": %s".formatted(this.id, e.getMessage()), e);
		}
	}
}
