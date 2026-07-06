package net.fabricmc.fabric.mixin.registry.sync;

import net.neoforged.neoforge.registries.BaseMappedRegistry;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.event.registry.FabricRegistry;

@Mixin(BaseMappedRegistry.class)
public abstract class BaseMappedRegistryMixin implements FabricRegistry {
}
