package net.fabricmc.fabric.mixin.networking;

import io.netty.channel.ChannelPipeline;
import net.neoforged.neoforge.network.filters.NetworkFilters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.network.HandlerNames;

import net.fabricmc.fabric.impl.networking.splitter.ChannelEncoderContextProvider;

@Mixin(NetworkFilters.class)
public class NetworkFiltersMixin {
	
	@Inject(method = "injectIfNecessary", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
	private static void injectContext(Connection connection, CallbackInfo ci) {
		ChannelPipeline pipeline = connection.channel().pipeline();

		pipeline.addAfter(HandlerNames.ENCODER, "fabric:context", new ChannelEncoderContextProvider());
	}
}
