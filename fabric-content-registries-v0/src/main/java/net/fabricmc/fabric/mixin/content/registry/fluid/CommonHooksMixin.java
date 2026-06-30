package net.fabricmc.fabric.mixin.content.registry.fluid;

import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.material.Fluid;

import net.fabricmc.loader.api.FabricLoader;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {

	@Inject(method = "getVanillaFluidType", at = @At(value = "NEW", target = "java/lang/RuntimeException"), cancellable = true)
	private static void useDefaultType(Fluid fluid, CallbackInfoReturnable<FluidType> cir) {
		boolean useDefault = FabricLoader.getInstance().getModContainer(fluid.builtInRegistryHolder().getKey().identifier().getNamespace())
				.map(c -> c.getMetadata().getCustomValue("sinytra:use_default_fluid_type"))
				.map(c -> c != null && c.getAsBoolean())
				.orElse(false);

		if (useDefault) {
			cir.setReturnValue(NeoForgeMod.EMPTY_TYPE.value());
		}
	}
}
