package net.fabricmc.fabric.mixin.networking.client;

import com.google.common.collect.ImmutableSet;
import net.neoforged.neoforge.client.network.registration.ClientNetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;

@Mixin(ClientNetworkRegistry.class)
public class ClientNetworkRegistryMixin {
	@Inject(method = "handleModdedPayload", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z"), cancellable = true)
	private static void preventDisconnect(ClientCommonPacketListener listener, ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
		if (NetworkingImpl.getCodec(packet.payload().type().id(), listener.protocol(), listener.flow()) != null) {
			ci.cancel();
		}
	}

	@ModifyVariable(method = "sendInitialListeningChannels", at = @At(value = "STORE"))
	private static ImmutableSet.Builder<Identifier> sendInitialFabricChannels(ImmutableSet.Builder<Identifier> nowListeningOn) {
		nowListeningOn.addAll(ClientConfigurationNetworking.getGlobalReceivers());
		return nowListeningOn;
	}
}
