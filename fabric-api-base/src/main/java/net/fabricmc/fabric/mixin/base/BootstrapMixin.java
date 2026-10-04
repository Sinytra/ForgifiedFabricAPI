package net.fabricmc.fabric.mixin.base;

import net.fabricmc.fabric.impl.base.registry.EarlyRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
	
	@Inject(method = "bootStrap()V", at = @At("TAIL"))
	private static void postBootstrap(CallbackInfo ci) {
		for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
			if (registry instanceof EarlyRegistry er) {
				er.gatherCallbacks();
			}
		}
	}
}
