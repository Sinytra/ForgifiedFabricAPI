package net.fabricmc.fabric.mixin.registry.sync;

import net.fabricmc.fabric.api.event.registry.FabricRegistry;

import net.minecraft.core.Registry;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(Registry.class)
public interface RegistryMixin extends FabricRegistry {
}
