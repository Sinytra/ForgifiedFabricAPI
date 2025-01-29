package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.negotiation.NegotiatedNetworkComponent;
import net.neoforged.neoforge.network.negotiation.NegotiationResult;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryComponent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.sinytra.fabric.networking_api.NeoNetworkRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(NetworkRegistry.class)
public class NetworkRegistryMixin {

    @Inject(method = "getCodec", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 0), cancellable = true)
    private static void getCodec(ResourceLocation id, ConnectionProtocol protocol, PacketFlow flow, CallbackInfoReturnable<StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload>> cir) {
        PayloadTypeRegistryImpl<? extends FriendlyByteBuf> registry = NeoNetworkRegistrar.getPayloadRegistry(protocol, flow);
        CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> fabricCodec = registry.get(id);
        if (fabricCodec != null) {
            cir.setReturnValue((StreamCodec) fabricCodec.codec());
        }
    }

    @ModifyReturnValue(method = "getCodec", at = @At(value = "RETURN", ordinal = 3))
    private static StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload> getFabricDynamicCodec(StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload> codec, ResourceLocation id, ConnectionProtocol protocol, PacketFlow flow) {
        if (codec == NeoNetworkRegistrar.DUMMY_CODEC) {
            PayloadTypeRegistryImpl<? extends FriendlyByteBuf> registry = NeoNetworkRegistrar.getPayloadRegistry(protocol, flow);
            CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> fabricCodec = registry.get(id);
            if (fabricCodec != null) {
                return (StreamCodec) fabricCodec.codec();
            }
        }
        return codec;
    }

    @Inject(method = "handleModdedPayload(Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V", ordinal = 1), cancellable = true)
    private static void preventDisconnectOnUnknownFabricPacketClient(ClientCommonPacketListener listener, ClientboundCustomPayloadPacket packet, CallbackInfo info) {
        if (NeoNetworkRegistrar.hasCodecFor(listener.protocol(), packet.type().flow(), packet.payload().type().id())) {
            info.cancel();
        }
    }

    @Inject(method = "handleModdedPayload(Lnet/minecraft/network/protocol/common/ServerCommonPacketListener;Lnet/minecraft/network/protocol/common/ServerboundCustomPayloadPacket;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"), cancellable = true)
    private static void preventDisconnectOnUnknownFabricPacketServer(ServerCommonPacketListener listener, ServerboundCustomPayloadPacket packet, CallbackInfo info) {
        if (NeoNetworkRegistrar.hasCodecFor(listener.protocol(), packet.type().flow(), packet.payload().type().id())) {
            info.cancel();
        }
    }

    @WrapOperation(
        method = {
            "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ServerCommonPacketListener;)V",
            "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;)V"
        },
        at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/registration/NetworkRegistry;hasChannel(Lnet/neoforged/neoforge/common/extensions/ICommonPacketListener;Lnet/minecraft/resources/ResourceLocation;)Z")
    )
    private static boolean includeFabricChannels(ICommonPacketListener listener, ResourceLocation location, Operation<Boolean> original) {
        // TODO Use original args that include the packet
        return original.call(listener, location) || NeoNetworkRegistrar.hasCodecFor(listener.protocol(), listener.flow() == PacketFlow.SERVERBOUND ? PacketFlow.CLIENTBOUND : PacketFlow.SERVERBOUND, location);
    }

    @ModifyVariable(method = "initializeNeoForgeConnection(Lnet/minecraft/network/protocol/configuration/ServerConfigurationPacketListener;Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/registration/NetworkPayloadSetup;from(Ljava/util/Map;)Lnet/neoforged/neoforge/network/registration/NetworkPayloadSetup;"), ordinal = 1)
    private static Map<ConnectionProtocol, NegotiationResult> preserveSendableChannels(Map<ConnectionProtocol, NegotiationResult> results, ServerConfigurationPacketListener listener, Map<ConnectionProtocol, Set<ModdedNetworkQueryComponent>> clientChannels) {
        Set<ModdedNetworkQueryComponent> configChannels = clientChannels.get(ConnectionProtocol.CONFIGURATION);
        if (configChannels != null && !configChannels.isEmpty()) {
            NegotiationResult negotiation = results.get(ConnectionProtocol.CONFIGURATION);
            List<NegotiatedNetworkComponent> components = new ArrayList<>(negotiation.components());
            configChannels.stream()
                    .filter(c -> components.stream().noneMatch(d -> c.id().equals(d.id())) && PayloadTypeRegistryImpl.CONFIGURATION_S2C.get(c.id()) != null)
                    .forEach(c -> components.add(new NegotiatedNetworkComponent(c.id(), c.version())));
            results.put(ConnectionProtocol.CONFIGURATION, new NegotiationResult(components, negotiation.success(), negotiation.failureReasons()));
        }
        Set<ModdedNetworkQueryComponent> playChannels = clientChannels.get(ConnectionProtocol.PLAY);
        if (playChannels != null && !playChannels.isEmpty()) {
            NegotiationResult negotiation = results.get(ConnectionProtocol.PLAY);
            List<NegotiatedNetworkComponent> components = new ArrayList<>(negotiation.components());
            playChannels.stream()
                .filter(c -> components.stream().noneMatch(d -> c.id().equals(d.id())) && PayloadTypeRegistryImpl.PLAY_S2C.get(c.id()) != null)
                .forEach(c -> components.add(new NegotiatedNetworkComponent(c.id(), c.version())));
            results.put(ConnectionProtocol.PLAY, new NegotiationResult(components, negotiation.success(), negotiation.failureReasons()));
        }
        return results;
    }
}
