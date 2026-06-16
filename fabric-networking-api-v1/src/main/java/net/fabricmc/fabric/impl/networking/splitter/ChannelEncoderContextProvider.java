package net.fabricmc.fabric.impl.networking.splitter;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.neoforged.neoforge.network.filters.DynamicChannelHandler;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import net.fabricmc.fabric.impl.networking.context.PacketContextSetter;

public class ChannelEncoderContextProvider extends MessageToMessageEncoder<Packet<?>> implements DynamicChannelHandler {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (ctx.pipeline().get("encoder") instanceof PacketContextSetter setter && setter.fabric_getPacketContext() != null) {
			ScopedValue.where(PacketContextImpl.VALUE, setter.fabric_getPacketContext())
					.run(() -> {
						try {
							super.write(ctx, msg, promise);
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}
					});
		} else {
			super.write(ctx, msg, promise);
		}
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet<?> msg, List<Object> out) {
		out.add(msg);
	}

	@Override
	public boolean isNecessary(Connection manager) {
		return true; // TODO?
	}
}
