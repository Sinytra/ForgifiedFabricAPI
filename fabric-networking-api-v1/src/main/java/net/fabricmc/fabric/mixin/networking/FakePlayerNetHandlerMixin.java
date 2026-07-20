package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.fabric.impl.networking.UntrackedPacketListener;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.neoforged.neoforge.common.util.FakePlayer$FakePlayerNetHandler")
public class FakePlayerNetHandlerMixin implements UntrackedPacketListener {
}
