package net.fabricmc.fabric.mixin.attachment;

import net.minecraft.core.MappedRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor {
    @Accessor
    boolean getFrozen();
}
