package net.fabricmc.fabric.mixin.transfer.registry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.MappedRegistry;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor {
    @Accessor
    boolean getFrozen();
}
