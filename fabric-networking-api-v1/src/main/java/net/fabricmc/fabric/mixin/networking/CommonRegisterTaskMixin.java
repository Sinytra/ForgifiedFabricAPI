package net.fabricmc.fabric.mixin.networking;

import java.util.HashSet;
import java.util.Set;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.network.configuration.CommonRegisterTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

@Mixin(CommonRegisterTask.class)
public class CommonRegisterTaskMixin {
	@ModifyExpressionValue(
			method = "run",
			at = @At(
					value = "INVOKE",
					target = "Lnet/neoforged/neoforge/network/registration/NetworkRegistry;getCommonPlayChannels(Lnet/minecraft/network/protocol/PacketFlow;)Ljava/util/Set;"
			)
	)
    private static Set<Identifier> sendFabricChannels(Set<Identifier> original) {
		Set<Identifier> all = new HashSet<>(original);
		all.addAll(ServerPlayNetworking.getGlobalReceivers());
		return all;
    }
}
