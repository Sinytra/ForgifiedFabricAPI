package org.sinytra.ffapi.mixin.fluids;

import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.fluids.FluidType;
import org.sinytra.ffapi.impl.fluids.FabricFluidTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.material.Fluid;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {
	@Inject(method = "getVanillaFluidType", at = @At(value = "NEW", target = "java/lang/RuntimeException"), cancellable = true)
	private static void getFabricVanillaFluidType(Fluid fluid, CallbackInfoReturnable<FluidType> cir) {
		FluidType fabricFluidType = FabricFluidTypes.getFluidType(fluid);
		if (fabricFluidType != null) {
			cir.setReturnValue(fabricFluidType);
		}
	}
}
