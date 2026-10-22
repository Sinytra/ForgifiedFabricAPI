package net.fabricmc.fabric.mixin.registry.sync;

import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DataPackRegistryEvent.NewRegistry.class)
public interface DataPackRegistryEventNewRegistryAccessor {
    @Invoker
    void invokeProcess();
}
