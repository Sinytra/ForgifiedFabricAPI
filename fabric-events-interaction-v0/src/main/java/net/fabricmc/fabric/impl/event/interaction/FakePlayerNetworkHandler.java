package net.fabricmc.fabric.impl.event.interaction;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.impl.networking.UntrackedNetworkHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public final class FakePlayerNetworkHandler extends ServerGamePacketListenerImpl implements UntrackedNetworkHandler {
	private static final Connection FAKE_CONNECTION = new FakeClientConnection();

	public FakePlayerNetworkHandler(ServerPlayer player) {
		super(player.getServer(), FAKE_CONNECTION, player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
	}

    @Override
    public void send(Packet<?> packet, @Nullable PacketSendListener listener) {}

    private static final class FakeClientConnection extends Connection {
		private final Channel channel;
		
		private FakeClientConnection() {
			super(PacketFlow.CLIENTBOUND);
			
			this.channel = new EmbeddedChannel();
		}
		
		public void setListenerForServerboundHandshake(PacketListener listener) {}

		@Override
		public void channelActive(ChannelHandlerContext channelHandlerContext) {}

		@Override
		public Channel channel() {
			return this.channel;
		}
	}
}
