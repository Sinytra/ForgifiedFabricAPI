package org.sinytra.fabric.networking_api;

import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.mixin.networking.accessor.NetworkRegistryAccessor;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NeoNetworkRegistrar {
    // Not our actual codec, see NetworkRegistryMixin
    public static final StreamCodec<?, ?> DUMMY_CODEC = StreamCodec.of((a, b) -> {
        throw new UnsupportedOperationException();
    }, a -> {
        throw new UnsupportedOperationException();
    });

    private final ConnectionProtocol protocol;

    private final Map<ResourceLocation, NeoPayloadHandler<?>> registeredPayloads = new HashMap<>();

    public NeoNetworkRegistrar(ConnectionProtocol protocol) {
        this.protocol = protocol;
    }

    public static boolean hasCodecFor(ConnectionProtocol protocol, PacketFlow flow, ResourceLocation id) {
        PayloadTypeRegistryImpl<? extends FriendlyByteBuf> registry = getPayloadRegistry(protocol, flow);
        return registry.get(id) != null;
    }

    public static PayloadTypeRegistryImpl<? extends FriendlyByteBuf> getPayloadRegistry(ConnectionProtocol protocol, PacketFlow flow) {
        if (protocol == ConnectionProtocol.PLAY) {
            return flow == PacketFlow.SERVERBOUND ? PayloadTypeRegistryImpl.PLAY_C2S : PayloadTypeRegistryImpl.PLAY_S2C;
        } else if (protocol == ConnectionProtocol.CONFIGURATION) {
            return flow == PacketFlow.SERVERBOUND ? PayloadTypeRegistryImpl.CONFIGURATION_C2S : PayloadTypeRegistryImpl.CONFIGURATION_S2C;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public <PAYLOAD extends CustomPacketPayload, CONTEXT, HANDLER> boolean registerGlobalReceiver(CustomPacketPayload.Type<PAYLOAD> type, PacketFlow packetFlow, HANDLER handler, Function<IPayloadContext, CONTEXT> ctxFactory, TriConsumer<HANDLER, PAYLOAD, CONTEXT> consumer) {
        NeoPayloadHandler<PAYLOAD> neoHandler = getOrRegisterNativeHandler(type);
        return neoHandler.registerGlobalHandler(packetFlow, handler, ctxFactory, consumer);
    }

    public <HANDLER> HANDLER unregisterGlobalReceiver(ResourceLocation id, PacketFlow flow) {
        NeoPayloadHandler<?> neoHandler = registeredPayloads.get(id);
        return neoHandler != null ? neoHandler.unregisterGlobalHandler(flow) : null;
    }

    public Set<ResourceLocation> getGlobalReceivers(PacketFlow flow) {
        return registeredPayloads.entrySet().stream()
            .filter(e -> e.getValue().hasGlobalHandler(flow))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    public <PAYLOAD extends CustomPacketPayload, CONTEXT, HANDLER> boolean registerLocalReceiver(CustomPacketPayload.Type<PAYLOAD> type, ICommonPacketListener listener, HANDLER handler, Function<IPayloadContext, CONTEXT> ctxFactory, TriConsumer<HANDLER, PAYLOAD, CONTEXT> consumer) {
        NeoPayloadHandler<PAYLOAD> neoHandler = getOrRegisterNativeHandler(type);
        return neoHandler.registerLocalReceiver(listener, handler, ctxFactory, consumer);
    }

    public <HANDLER> HANDLER unregisterLocalReceiver(ResourceLocation id, ICommonPacketListener listener) {
        NeoPayloadHandler<?> neoHandler = registeredPayloads.get(id);
        return neoHandler != null ? neoHandler.unregisterLocalHandler(listener) : null;
    }

    public Set<ResourceLocation> getLocalReceivers(ICommonPacketListener listener) {
        return registeredPayloads.entrySet().stream()
            .filter(e -> e.getValue().hasLocalHandler(listener))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    public Set<ResourceLocation> getLocalSendable(ICommonPacketListener listener) {
        NetworkPayloadSetup payloadSetup = listener.getConnection().channel().attr(NetworkRegistryAccessor.getAttributePayloadSetup()).get();
        if (payloadSetup == null) {
            return Set.of();
        }
        return payloadSetup.channels().get(this.protocol).keySet();
    }

    @SuppressWarnings("unchecked")
    private <PAYLOAD extends CustomPacketPayload> NeoPayloadHandler<PAYLOAD> getOrRegisterNativeHandler(CustomPacketPayload.Type<PAYLOAD> type) {
        return (NeoPayloadHandler<PAYLOAD>) registeredPayloads.computeIfAbsent(type.id(), k -> {
            NeoPayloadHandler<PAYLOAD> handler = new NeoPayloadHandler<>();
            boolean setup = NetworkRegistryAccessor.getSetup();

            NetworkRegistryAccessor.setSetup(false);
            NetworkRegistry.register(type, (StreamCodec<? super FriendlyByteBuf, PAYLOAD>) DUMMY_CODEC, handler, List.of(protocol), Optional.empty(), "1.0", true);
            NetworkRegistryAccessor.setSetup(setup);

            // TODO Send registration message when registering late
            return handler;
        });
    }

    public static class NeoPayloadHandler<PAYLOAD extends CustomPacketPayload> implements IPayloadHandler<PAYLOAD> {
        private final Map<PacketFlow, NeoSubHandler<PAYLOAD, ?, ?>> globalReceivers = new HashMap<>();
        private final Map<ICommonPacketListener, NeoSubHandler<PAYLOAD, ?, ?>> localReceivers = new HashMap<>();

        @Override
        public void handle(PAYLOAD arg, IPayloadContext context) {
            NeoSubHandler globalHandler = globalReceivers.get(context.flow());
            if (globalHandler != null) {
                context.enqueueWork(() -> globalHandler.consumer().accept(globalHandler.handler(), arg, globalHandler.ctxFactory().apply(context)));
            }
            NeoSubHandler localHandler = localReceivers.get(context.listener());
            if (localHandler != null) {
                context.enqueueWork(() -> localHandler.consumer().accept(localHandler.handler(), arg, localHandler.ctxFactory().apply(context)));
            }
        }

        public boolean hasGlobalHandler(PacketFlow flow) {
            return globalReceivers.containsKey(flow);
        }

        public <CONTEXT, HANDLER> boolean registerGlobalHandler(PacketFlow flow, HANDLER original, Function<IPayloadContext, CONTEXT> ctxFactory, TriConsumer<HANDLER, PAYLOAD, CONTEXT> consumer) {
            if (!hasGlobalHandler(flow)) {
                globalReceivers.put(flow, new NeoSubHandler<>(original, ctxFactory, consumer));
                return true;
            }
            return false;
        }

        public boolean hasLocalHandler(ICommonPacketListener listener) {
            return localReceivers.containsKey(listener);
        }

        public <CONTEXT, HANDLER> boolean registerLocalReceiver(ICommonPacketListener listener, HANDLER original, Function<IPayloadContext, CONTEXT> ctxFactory, TriConsumer<HANDLER, PAYLOAD, CONTEXT> consumer) {
            if (!hasLocalHandler(listener)) {
                localReceivers.put(listener, new NeoSubHandler<>(original, ctxFactory, consumer));
                return true;
            }
            return false;
        }

        @Nullable
        public <HANDLER> HANDLER unregisterGlobalHandler(PacketFlow flow) {
            NeoSubHandler subHandler = globalReceivers.remove(flow);
            return subHandler != null ? (HANDLER) subHandler.handler() : null;
        }

        @Nullable
        public <HANDLER> HANDLER unregisterLocalHandler(ICommonPacketListener listener) {
            NeoSubHandler subHandler = localReceivers.remove(listener);
            return subHandler != null ? (HANDLER) subHandler.handler() : null;
        }
    }

    record NeoSubHandler<PAYLOAD extends CustomPacketPayload, CONTEXT, HANDLER>(HANDLER handler, Function<IPayloadContext, CONTEXT> ctxFactory, TriConsumer<HANDLER, PAYLOAD, CONTEXT> consumer) { }
}
