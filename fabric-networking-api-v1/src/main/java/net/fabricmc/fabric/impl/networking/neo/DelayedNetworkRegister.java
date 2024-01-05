package net.fabricmc.fabric.impl.networking.neo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.fabricmc.fabric.impl.networking.CommonPacketsImpl;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.IPacketHandler;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.payload.ResolvablePayload;
import net.fabricmc.fabric.impl.networking.payload.ResolvedPayload;
import net.fabricmc.fabric.impl.networking.payload.TypedPayload;
import net.fabricmc.fabric.impl.networking.payload.UntypedPayload;

public class DelayedNetworkRegister<VANILLA, INTERNAL, HOST> {
	private final Function<IPacketHandler, VANILLA> handlerUnwrapper;
	private final Function<VANILLA, HOST> serverGetter;
	private final FabricReceiverCallback<VANILLA, INTERNAL, HOST> fabricReceiverCallback;

	private final Map<Identifier, Consumer<IPayloadRegistrar>> registerQueue = new HashMap<>();
	private final Set<Identifier> registeredReceivers = new HashSet<>();

	private final Multimap<VANILLA, Identifier> clientSpecificHandlers = HashMultimap.create();

	public <B extends FabricPacket> DelayedNetworkRegister(Function<IPacketHandler, VANILLA> handlerUnwrapper, Function<VANILLA, HOST> serverGetter, FabricReceiverCallback<VANILLA, INTERNAL, HOST> fabricReceiverCallback) {
		this.handlerUnwrapper = handlerUnwrapper;
		this.serverGetter = serverGetter;
		this.fabricReceiverCallback = fabricReceiverCallback;
	}

	public interface FabricReceiverCallback<VANILLA, INTERNAL, HOST> {
		void receive(VANILLA handler, HOST server, PacketSender sender, INTERNAL internalHandler, ResolvedPayload payload, ConfigurationPayloadContext context);
	}

	public Set<Identifier> getChannels() {
		return this.registeredReceivers;
	}
	
	public Set<Identifier> getReceived(VANILLA handler) {
		Set<Identifier> receivers = new HashSet<>(getChannels());
		receivers.addAll(this.clientSpecificHandlers.get(handler));
		return receivers;
	}
	
	public Set<Identifier> getSendable(VANILLA handler) {
		throw new UnsupportedOperationException(); // TODO
	}
	
	public PacketSender getSender(VANILLA handler) {
		return (PacketSender) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public void apply(RegisterPayloadHandlerEvent event) {
		Multimap<String, Consumer<IPayloadRegistrar>> byNamespace = HashMultimap.create();
		this.registerQueue.forEach((id, consumer) -> byNamespace.put(id.getNamespace(), consumer));
		this.registerQueue.clear();
		byNamespace.asMap().forEach((namespace, consumers) -> {
			IPayloadRegistrar registrar = event.registrar(namespace)
					.versioned(String.valueOf(CommonPacketsImpl.PACKET_VERSION_1));
			consumers.forEach(c -> c.accept(registrar));
		});
	}

	public boolean registerGlobalReceiver(Identifier identifier, ResolvablePayload.Handler<INTERNAL> upstream) {
		return registerGlobalReceiver(identifier, this.fabricReceiverCallback, upstream);
	}

	private boolean registerGlobalReceiver(Identifier identifier, FabricReceiverCallback<VANILLA, INTERNAL, HOST> fabricReceiverCallback, ResolvablePayload.Handler<INTERNAL> upstream) {
		return registerGlobalReceiver(identifier, buf -> new UntypedPayload(identifier, buf), fabricReceiverCallback, upstream);
	}

	public <P extends FabricPacket> boolean registerGlobalReceiver(PacketType<P> type, ResolvablePayload.Handler<INTERNAL> upstream) {
		return registerGlobalReceiver(type.getId(), buf -> new TypedPayload(type.read(buf)), this.fabricReceiverCallback, upstream);
	}

	private boolean registerGlobalReceiver(Identifier identifier, PacketByteBuf.PacketReader<ResolvedPayload> payloadFactory, FabricReceiverCallback<VANILLA, INTERNAL, HOST> fabricReceiverCallback, ResolvablePayload.Handler<INTERNAL> upstream) {
		if (this.registerQueue.containsKey(identifier)) {
			return false;
		}
		this.registerQueue.put(identifier, registrar -> registrar.configuration(identifier, payloadFactory, (payload, context) -> {
			VANILLA handler = this.handlerUnwrapper.apply(context.packetHandler());
			HOST host = this.serverGetter.apply(handler);
			PacketSender sender = getSender(handler);
			fabricReceiverCallback.receive(handler, host, sender, upstream.internal(), payload, context);
		}));
		this.registeredReceivers.add(identifier);
		return true;
	}

	public ResolvablePayload.Handler<INTERNAL> unregisterGlobalReceiver(Identifier channelName) {
		throw new UnsupportedOperationException(); // TODO
	}

	public ResolvablePayload.Handler<INTERNAL> unregisterTypedGlobalReceiver(Identifier channelName) {
		throw new UnsupportedOperationException(); // TODO
	}

	public boolean registerReceiver(VANILLA networkHandler, Identifier identifier, ResolvablePayload.Handler<INTERNAL> upstream) {
		if (registerGlobalReceiver(identifier, (handler, server, sender, internalHandler, payload, context) -> {
			if (this.clientSpecificHandlers.get(handler).contains(identifier)) {
				this.fabricReceiverCallback.receive(handler, server, sender, internalHandler, payload, context);
			}
		}, upstream)) {
			this.clientSpecificHandlers.put(networkHandler, identifier);
			return true;
		}
		return false;
	}
	
	public ResolvablePayload.Handler<INTERNAL> unregisterReceiver(VANILLA networkHandler, Identifier channelName) {
		throw new UnsupportedOperationException(); // TODO
	}
	
	public <P extends FabricPacket> ResolvablePayload.Handler<INTERNAL> unregisterReceiver(VANILLA networkHandler, PacketType<P> type) {
		throw new UnsupportedOperationException(); // TODO
	}
}
