package net.fabricmc.fabric.mixin.content.registry;

import net.fabricmc.fabric.impl.content.registry.DataMapModifications;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseMappedRegistry.class)
public abstract class BaseRegistryMixin<T> {
    @Inject(at = @At("HEAD"), method = "getData", cancellable = true)
    private <A> void getDataMapConsideringFAPI(DataMapType<T, A> type, ResourceKey<T> key, CallbackInfoReturnable<A> cir) {
        DataMapModifications.modify((Registry<T>) this, type, key, cir);
    }
}
