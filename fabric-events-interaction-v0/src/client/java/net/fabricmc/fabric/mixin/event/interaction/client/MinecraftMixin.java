/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.event.interaction.client;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;

import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Unique
	private boolean attackCancelled;

	@Shadow
	public LocalPlayer player;

	@Shadow
	@Final
	public Options options;

	@Shadow
	@Nullable
	public MultiPlayerGameMode gameMode;

	@Shadow
	@Nullable
	public ClientLevel level;

	@Inject(
			method = "handleKeybinds",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
					ordinal = 0
			)
	)
	private void injectHandleInputEventsForPreAttackCallback(CallbackInfo ci) {
		int attackKeyPressCount = ((KeyMappingAccessor) options.keyAttack).fabric_getTimesPressed();

		if (options.keyAttack.isDown() || attackKeyPressCount != 0) {
			attackCancelled = ClientPreAttackCallback.EVENT.invoker().onClientPlayerPreAttack(
					(Minecraft) (Object) this, player, attackKeyPressCount
			);
		} else {
			attackCancelled = false;
		}
	}

	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void injectDoAttackForCancelling(CallbackInfoReturnable<Boolean> cir) {
		if (attackCancelled) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void injectHandleBlockBreakingForCancelling(boolean breaking, CallbackInfo ci) {
		if (attackCancelled) {
			if (gameMode != null) {
				gameMode.stopDestroyBlock();
			}

			ci.cancel();
		}
	}
}
