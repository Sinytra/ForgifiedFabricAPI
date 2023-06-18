package net.fabricmc.fabric.mixin.screenhandler;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(NetworkHooks.class)
public class NetworkHooksMixin {

    @Inject(method = "openScreen(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void injectOpenScreen(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter, CallbackInfo ci, int openContainerId, FriendlyByteBuf extraData, FriendlyByteBuf output, AbstractContainerMenu menu) {
        // Allows writeScreenOpeningData to access the current menu through the player
        if (containerSupplier instanceof ExtendedScreenHandlerFactory extendedFactory) {
            player.containerMenu = menu;

            FriendlyByteBuf extendedExtraData = new FriendlyByteBuf(Unpooled.buffer());
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
