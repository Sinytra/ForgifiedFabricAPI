package net.fabricmc.fabric.mixin.serialization;

import net.fabricmc.fabric.api.serialization.v1.value.FabricValueInput;

import net.neoforged.neoforge.common.extensions.ValueInputExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ValueInputExtension.class)
public interface ValueInputExtensionMixin extends FabricValueInput {
}
