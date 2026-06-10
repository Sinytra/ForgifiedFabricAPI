package net.fabricmc.fabric.mixin.client.gametest;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.client.gametest.util.DedicatedServerImplUtil;import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.Main;

@Mixin(Main.class)
public class MainMixin {
	@WrapOperation(method = "main", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/server/loading/ServerModLoader;load(Z)V"))
	private static void skipServerModLoading(boolean isGameTest, Operation<Void> original) {
		if (DedicatedServerImplUtil.isRunningServer) {
			return;
		}
		original.call(isGameTest);
	}
}
