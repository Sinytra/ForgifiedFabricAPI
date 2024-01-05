package net.fabricmc.fabric.mixin.networking.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.listener.ClientCommonPacketListener;

@Mixin(targets = "net.neoforged.neoforge.network.registration.NetworkRegistry$ClientPacketHandler")
public interface ClientPacketHandlerAccessor {
	@Accessor
	ClientCommonPacketListener getListener();
}
