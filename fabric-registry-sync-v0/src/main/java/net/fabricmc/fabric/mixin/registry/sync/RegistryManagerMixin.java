package net.fabricmc.fabric.mixin.registry.sync;

import net.fabricmc.fabric.impl.registry.sync.FabricRegistryInit;

import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegistryManager.class)
public class RegistryManagerMixin {
	
	@Inject(method = "postNewRegistryEvent", at = @At("HEAD"), remap = false)
	private static void beforePostNewRegistryEvent(CallbackInfo ci) {
		FabricRegistryInit.submitRegistries();
	}
}
