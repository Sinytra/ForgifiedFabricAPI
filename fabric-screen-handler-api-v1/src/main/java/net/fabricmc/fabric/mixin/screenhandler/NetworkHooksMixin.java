package net.fabricmc.fabric.mixin.screenhandler;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

@Mixin(NetworkHooks.class)
public class NetworkHooksMixin {

    @Inject(method = "openScreen(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/screen/NamedScreenHandlerFactory;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/screen/NamedScreenHandlerFactory;createMenu(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/screen/ScreenHandler;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void injectOpenScreen(ServerPlayerEntity player, NamedScreenHandlerFactory containerSupplier, Consumer<PacketByteBuf> extraDataWriter, CallbackInfo ci, int openContainerId, PacketByteBuf extraData, PacketByteBuf output, ScreenHandler menu) {
        // Allows writeScreenOpeningData to access the current menu through the player
        if (containerSupplier instanceof ExtendedScreenHandlerFactory extendedFactory) {
            player.currentScreenHandler = menu;

			PacketByteBuf extendedExtraData = new PacketByteBuf(Unpooled.buffer());
            extendedFactory.writeScreenOpeningData(player, extendedExtraData);
            extendedExtraData.readerIndex(0);

            output.clear();
            output.writeVarInt(extendedExtraData.readableBytes());
            output.writeBytes(extendedExtraData);

            // Run sanity check again
            if (output.readableBytes() > 32600 || output.readableBytes() < 1) {
                throw new IllegalArgumentException("Invalid PacketBuffer for openGui, found "+ output.readableBytes()+ " bytes");
            }
        }
    }
}
