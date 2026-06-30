package net.fabricmc.fabric.mixin.networking.client;

import java.util.HashSet;
import java.util.Set;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;

@Mixin(NetworkRegistry.class)
public class NetworkRegistryClientMixin {
	@Inject(
			method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/neoforged/neoforge/network/registration/NetworkRegistry;hasChannel(Lnet/neoforged/neoforge/common/extensions/ICommonPacketListener;Lnet/minecraft/resources/Identifier;)Z"
			),
			cancellable = true
	)
	private static void checkFabricClientPacket(Packet<?> packet, ClientCommonPacketListener listener, CallbackInfo ci) {
		ServerboundCustomPayloadPacket customPayloadPacket = (ServerboundCustomPayloadPacket) packet;
		Type<?> type = customPayloadPacket.payload().type();

		if (listener.protocol() == ConnectionProtocol.CONFIGURATION && ClientConfigurationNetworking.canSend(type)) {
			ci.cancel();
		}

		if (listener.protocol() == ConnectionProtocol.PLAY && ClientPlayNetworking.canSend(type)) {
			ci.cancel();
		}

		if (NetworkingImpl.getCodec(type.id(), listener.protocol(), listener.flow()) != null) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = "onCommonRegister", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/registration/NetworkRegistry;getCommonPlayChannels(Lnet/minecraft/network/protocol/PacketFlow;)Ljava/util/Set;"))
	private static Set<Identifier> addFabricChannels(Set<Identifier> original) {
		Set<Identifier> all = new HashSet<>(original);
		all.addAll(ClientPlayNetworking.getGlobalReceivers());
		return all;
	}
}
