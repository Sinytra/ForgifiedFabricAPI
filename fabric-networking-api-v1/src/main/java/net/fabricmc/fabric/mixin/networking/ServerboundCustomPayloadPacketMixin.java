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

package net.fabricmc.fabric.mixin.networking;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.fabricmc.fabric.impl.networking.FabricCustomPayloadStreamCodec;
import net.fabricmc.fabric.impl.networking.GenericPayloadAccessor;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin implements GenericPayloadAccessor {
	@Shadow
	@Final
	private CustomPacketPayload payload;

	@WrapOperation(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/network/protocol/PacketFlow;)Lnet/minecraft/network/codec/StreamCodec;",
					ordinal = 0
			)
	)
	private static StreamCodec<FriendlyByteBuf, CustomPacketPayload> wrapCodec(CustomPacketPayload.FallbackProvider<FriendlyByteBuf> unknownCodecFactory, List<CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, ?>> types, ConnectionProtocol protocol, PacketFlow flow, Operation<StreamCodec<FriendlyByteBuf, CustomPacketPayload>> original) {
		StreamCodec<FriendlyByteBuf, CustomPacketPayload> codec = original.call(unknownCodecFactory, types, protocol, flow);
		FabricCustomPayloadStreamCodec<FriendlyByteBuf> fabricCodec = (FabricCustomPayloadStreamCodec<FriendlyByteBuf>) codec;
		fabricCodec.fabric_setCustomPayloadTypeProvider((friendlyByteBuf, identifier) -> {
			return (CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, ? extends CustomPacketPayload>) (Object) PayloadTypeRegistryImpl.SERVERBOUND_PLAY.get(identifier);
		});
		return codec;
	}

	@WrapOperation(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/network/protocol/PacketFlow;)Lnet/minecraft/network/codec/StreamCodec;",
					ordinal = 1
			)
	)
	private static StreamCodec<FriendlyByteBuf, CustomPacketPayload> wrapConfigCodec(CustomPacketPayload.FallbackProvider<FriendlyByteBuf> unknownCodecFactory, List<CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, ?>> types, ConnectionProtocol protocol, PacketFlow flow, Operation<StreamCodec<FriendlyByteBuf, CustomPacketPayload>> original) {
		StreamCodec<FriendlyByteBuf, CustomPacketPayload> codec = original.call(unknownCodecFactory, types, protocol, flow);
		FabricCustomPayloadStreamCodec<FriendlyByteBuf> fabricCodec = (FabricCustomPayloadStreamCodec<FriendlyByteBuf>) codec;
		fabricCodec.fabric_setCustomPayloadTypeProvider((friendlyByteBuf, identifier) -> {
			return PayloadTypeRegistryImpl.SERVERBOUND_CONFIGURATION.get(identifier);
		});
		return codec;
	}

	@Override
	public CustomPacketPayload fabric_payload() {
		return this.payload;
	}
}
