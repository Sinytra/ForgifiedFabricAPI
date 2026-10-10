package org.sinytra.ffapi.mixin.fluids;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityFluidInteraction;

import net.neoforged.neoforge.fluids.FluidType;
import org.sinytra.ffapi.impl.fluids.FabricFluidTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Optional;

@Mixin(EntityFluidInteraction.class)
public class EntityFluidInteractionMixin {
	@Inject(method = "getFluidTypeByTag", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V"), cancellable = true)
	private static void getFabricVanillaFluidType(TagKey<Fluid> fluidTag, CallbackInfoReturnable<FluidType> cir) {
		BuiltInRegistries.FLUID.get(fluidTag).ifPresent(named->{
			List<Holder<Fluid>> contents = named.contents();
			if (contents.isEmpty()) return;
			FluidType fabricFluidType = FabricFluidTypes.getFluidType(contents.getFirst().value());
			if (fabricFluidType != null) {
				cir.setReturnValue(fabricFluidType);
			}
		});
	}
}
