package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

@Mixin(NetworkRegistry.class)
public class NetworkRegistryMixin {

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
		if (listener instanceof ServerGamePacketListenerImpl list && ServerPlayNetworking.canSend(list, type)) {
			ci.cancel();
		}
		if (listener instanceof ServerConfigurationPacketListenerImpl list && ServerConfigurationNetworking.canSend(list, type)) {
			ci.cancel();
		}

		ci.cancel(); // TODO
	}

	@Inject(
			method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/neoforged/neoforge/network/registration/NetworkRegistry;hasChannel(Lnet/neoforged/neoforge/common/extensions/ICommonPacketListener;Lnet/minecraft/resources/Identifier;)Z"
			),
			cancellable = true
	)
	private static void checkFabricClientPacket(Packet<?> packet, ClientCommonPacketListener listener, CallbackInfo ci) {
		ci.cancel();
	}

	@ModifyReturnValue(method = "hasAdhocChannel", at = @At("RETURN"))
	private static boolean fabric_hasAdhocChannel(boolean original) {
		return true; // TODO
	}
}
