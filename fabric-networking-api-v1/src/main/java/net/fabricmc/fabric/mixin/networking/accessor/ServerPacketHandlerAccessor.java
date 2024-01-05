package net.fabricmc.fabric.mixin.networking.accessor;

import net.minecraft.network.listener.ServerCommonPacketListener;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.neoforged.neoforge.network.registration.NetworkRegistry$ServerPacketHandler")
public interface ServerPacketHandlerAccessor {
	@Accessor
	ServerCommonPacketListener getListener();
}
