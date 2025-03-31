package org.sinytra.fabric.networking_api.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jetbrains.annotations.Nullable;
import org.sinytra.fabric.networking_api.NeoCommonNetworking;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Set;

public class NeoClientPlayNetworking {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static ICommonPacketListener tempPacketListener;

    public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        NeoCommonNetworking.assertPayloadType(PayloadTypeRegistryImpl.PLAY_S2C, type.id(), PacketFlow.CLIENTBOUND, ConnectionProtocol.PLAY);
        return NeoCommonNetworking.PLAY_REGISTRY.registerGlobalReceiver(type, PacketFlow.CLIENTBOUND, handler, ClientNeoContextWrapper::new, ClientPlayNetworking.PlayPayloadHandler::receive);
    }

    public static ClientPlayNetworking.PlayPayloadHandler<?> unregisterGlobalReceiver(ResourceLocation id) {
        return NeoCommonNetworking.PLAY_REGISTRY.unregisterGlobalReceiver(id, PacketFlow.CLIENTBOUND);
    }

    public static Set<ResourceLocation> getGlobalReceivers() {
        return NeoCommonNetworking.PLAY_REGISTRY.getGlobalReceivers(PacketFlow.CLIENTBOUND);
    }

    public static <T extends CustomPacketPayload> boolean registerReceiver(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        NeoCommonNetworking.assertPayloadType(PayloadTypeRegistryImpl.PLAY_S2C, type.id(), PacketFlow.CLIENTBOUND, ConnectionProtocol.PLAY);
        ICommonPacketListener listener = Objects.requireNonNull(getClientListener(), "Cannot register receiver while not in game!");
        return NeoCommonNetworking.PLAY_REGISTRY.registerLocalReceiver(type, listener, handler, ClientNeoContextWrapper::new, ClientPlayNetworking.PlayPayloadHandler::receive);
    }

    public static ClientPlayNetworking.PlayPayloadHandler<?> unregisterReceiver(ResourceLocation id) {
        ICommonPacketListener listener = Objects.requireNonNull(getClientListener(), "Cannot unregister receiver while not in game!");
        return NeoCommonNetworking.PLAY_REGISTRY.unregisterLocalReceiver(id, listener);
    }

    public static Set<ResourceLocation> getReceived() throws IllegalStateException {
        ICommonPacketListener listener = Objects.requireNonNull(getClientListener(), "Cannot get a list of channels the client can receive packets on while not in game!");
        return NeoCommonNetworking.PLAY_REGISTRY.getLocalReceivers(listener);
    }

    public static Set<ResourceLocation> getSendable() throws IllegalStateException {
        ICommonPacketListener listener = Objects.requireNonNull(getClientListener(), "Cannot get a list of channels the server can receive packets on while not in game!");
        return NeoCommonNetworking.PLAY_REGISTRY.getLocalSendable(listener);
    }

    public static boolean canSend(ResourceLocation channelName) throws IllegalArgumentException {
        return NetworkRegistry.hasChannel(Minecraft.getInstance().getConnection(), channelName);
    }

    public static PacketSender getSender() {
        return new NeoClientPacketSender(Minecraft.getInstance().getConnection().getConnection());
    }

    public static void onServerReady(ClientPacketListener handler, Minecraft client) {
        NeoClientPacketSender packetSender = new NeoClientPacketSender(handler.getConnection());
        try {
            ClientPlayConnectionEvents.JOIN.invoker().onPlayReady(handler, packetSender, client);
        } catch (RuntimeException e) {
            LOGGER.error("Exception thrown while invoking ClientPlayConnectionEvents.JOIN", e);
        }

        MinecraftRegisterPayload registerPacket = new MinecraftRegisterPayload(NeoCommonNetworking.PLAY_REGISTRY.getGlobalReceivers(PacketFlow.CLIENTBOUND));
        if (!registerPacket.newChannels().isEmpty()) {
            packetSender.sendPacket(registerPacket);   
        }
    }

    @Nullable
    private static ICommonPacketListener getClientListener() {
        // Since Minecraft can be a bit weird, we need to check for the play addon in a few ways:
        // If the client's player is set this will work
        if (Minecraft.getInstance().getConnection() != null) {
            tempPacketListener = null; // Shouldn't need this anymore
            return Minecraft.getInstance().getConnection();
        }

        // We haven't hit the end of onGameJoin yet, use our backing field here to access the network handler
        if (tempPacketListener != null) {
            return tempPacketListener;
        }

        // We are not in play stage
        return null;
    }
    
    public static void setTempPacketListener(ICommonPacketListener listener) {
        tempPacketListener = listener;
    }

    private record ClientNeoContextWrapper(IPayloadContext context) implements ClientPlayNetworking.Context {
        @Override
        public Minecraft client() {
            return Minecraft.getInstance();
        }

        @Override
        public LocalPlayer player() {
            return (LocalPlayer) context.player();
        }

        @Override
        public PacketSender responseSender() {
            return new NeoClientPacketSender(context.connection());
        }
    }
}
