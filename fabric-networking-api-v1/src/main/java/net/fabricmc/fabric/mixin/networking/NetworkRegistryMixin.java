package net.fabricmc.fabric.mixin.networking;

import java.util.Set;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.ChannelHandlerContext;
import net.neoforged.neoforge.network.registration.NetworkChannel;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;

@Mixin(NetworkRegistry.class)
public class NetworkRegistryMixin {
	@Unique
	private static ChannelHandlerContext fabric_context;

	@Inject(
			method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ServerCommonPacketListener;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/neoforged/neoforge/network/registration/NetworkRegistry;hasChannel(Lnet/neoforged/neoforge/common/extensions/ICommonPacketListener;Lnet/minecraft/resources/Identifier;)Z"
			),
			cancellable = true
	)
	private static void checkFabricPacket(Packet<?> packet, ServerCommonPacketListener listener, CallbackInfo ci) {
		ClientboundCustomPayloadPacket customPayloadPacket = (ClientboundCustomPayloadPacket) packet;
		Type<?> type = customPayloadPacket.payload().type();

		if (listener instanceof ServerConfigurationPacketListenerImpl impl && ServerConfigurationNetworking.canSend(impl, type)) {
			ci.cancel();
		}

		if (listener instanceof ServerGamePacketListenerImpl impl && ServerPlayNetworking.canSend(impl, type)) {
			ci.cancel();
		}

		if (NetworkingImpl.getCodec(type.id(), listener.protocol(), listener.flow()) != null) {
			ci.cancel();
		}
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "getCodec", at = @At("HEAD"), cancellable = true)
	private static void getFabricCodec(Identifier id, ConnectionProtocol protocol, PacketFlow flow, CallbackInfoReturnable<StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload>> cir) {
		CustomPacketPayload.@Nullable TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> typeAndCodec = NetworkingImpl.getCodec(id, protocol, flow);
		if (typeAndCodec != null) {
			cir.setReturnValue((StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload>) typeAndCodec.codec());
		}
	}
	
	@Inject(method = "filterGameBundlePackets", at = @At("HEAD"))
	private static void captureContext(ChannelHandlerContext context, Iterable<Packet<?>> packets, CallbackInfoReturnable<?> cir) {
		fabric_context = context;
	}

	@ModifyExpressionValue(
			method = "lambda$filterGameBundlePackets$0",
			at = @At(
					value = "INVOKE",
					target = "Lnet/neoforged/neoforge/network/registration/NetworkPayloadSetup;getChannel(Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/resources/Identifier;)Lnet/neoforged/neoforge/network/registration/NetworkChannel;"
			)
	)
	private static NetworkChannel checkFabricChannels(NetworkChannel channel, @Local(name = "id") Identifier id) {
		if (channel == null && fabric_context.channel().hasAttr(NetworkingImpl.SENDABLE_CHANNELS)) {
			Set<Identifier> fabricChannels = fabric_context.channel().attr(NetworkingImpl.SENDABLE_CHANNELS).get();
			if (fabricChannels.contains(id)) {
				return new NetworkChannel(id, "1");
			}
		}
		return channel;
	}
}
