package net.fabricmc.fabric.mixin.registry.sync;

import net.neoforged.neoforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.resources.Identifier;

@Mixin(RegistryManager.class)
public interface RegistryManagerAccessor {
    @Invoker
    static void invokeTrackModdedRegistry(Identifier registry) {
        throw new UnsupportedOperationException();
    }
}
