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

package net.fabricmc.fabric.mixin.entity.event;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.util.EventResult;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Shadow
	public abstract boolean isDeadOrDying();

	@Shadow
	public abstract Optional<BlockPos> getSleepingPos();

	@WrapOperation(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;killedEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)Z"))
	private boolean onEntityKilledOther(Entity entity, ServerLevel serverLevel, @Nullable LivingEntity attacker, DamageSource damageSource, Operation<Boolean> original) {
		boolean result = original.call(entity, serverLevel, attacker, damageSource);
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.invoker().afterKilledOtherEntity(serverLevel, entity, attacker, damageSource);
		return result;
	}

	@Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
	private void notifyDeath(DamageSource source, CallbackInfo ci) {
		ServerLivingEntityEvents.AFTER_DEATH.invoker().afterDeath((LivingEntity) (Object) this, source);
	}

	@Redirect(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z", ordinal = 1))
	boolean beforeEntityKilled(LivingEntity livingEntity, ServerLevel level, DamageSource source, float amount) {
		return isDeadOrDying() && ServerLivingEntityEvents.ALLOW_DEATH.invoker().allowDeath(livingEntity, source, amount);
	}

	@Inject(method = "startSleeping", at = @At("RETURN"))
	private void onSleep(BlockPos pos, CallbackInfo info) {
		EntitySleepEvents.START_SLEEPING.invoker().onStartSleeping((LivingEntity) (Object) this, pos);
	}

	@Inject(method = "stopSleeping", at = @At("HEAD"))
	private void onWakeUp(CallbackInfo info) {
		BlockPos sleepingPos = getSleepingPos().orElse(null);

		// If actually asleep - this method is often called with data loading, syncing etc. "just to be sure"
		if (sleepingPos != null) {
			EntitySleepEvents.STOP_SLEEPING.invoker().onStopSleeping((LivingEntity) (Object) this, sleepingPos);
		}
	}

	@Dynamic("lambda$checkBedExists$0: Synthetic lambda body for Optional.map in checkBedExists")
	@Inject(method = "lambda$checkBedExists$0", at = @At("RETURN"), cancellable = true)
	private void onIsSleepingInBed(BlockPos sleepingPos, CallbackInfoReturnable<Boolean> info) {
		BlockState bedState = ((LivingEntity) (Object) this).level().getBlockState(sleepingPos);
		EventResult result = EntitySleepEvents.ALLOW_BED.invoker().allowBed((LivingEntity) (Object) this, sleepingPos, bedState, info.getReturnValueZ());

		if (result != EventResult.PASS) {
			info.setReturnValue(result.allowAction());
		}
	}

	@ModifyReturnValue(method = "getBedOrientation", at = @At("TAIL"))
	private Direction onGetSleepingDirection(Direction sleepingDirection, @Local BlockPos sleepingPos) {
		return EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.invoker().modifySleepDirection((LivingEntity) (Object) this, sleepingPos, sleepingDirection);
	}

	// This is needed 1) so that the vanilla logic in wakeUp runs for modded beds and 2) for the injector below.
	// The injector is shared because lambda$stopSleeping$23 and sleep share much of the structure here.
	@Dynamic("lambda$stopSleeping$0: Synthetic lambda body for Optional.ifPresent in stopSleeping")
	@ModifyExpressionValue(method = {"lambda$stopSleeping$0", "startSleeping"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isBed(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)Z"))
	private boolean modifyBedForOccupiedState(boolean vanillaResult, @Local(argsOnly = true) BlockPos sleepingPos, @Local BlockState state) {
		EventResult result = EntitySleepEvents.ALLOW_BED.invoker().allowBed((LivingEntity) (Object) this, sleepingPos, state, vanillaResult);

		// If a valid bed, replace with vanilla red bed so that the vanilla instanceof check succeeds.
		return result.allowAction(false) || vanillaResult;
	}

	// The injector is shared because lambda$stopSleeping$23 and sleep share much of the structure here.
	@Dynamic("lambda$stopSleeping$0: Synthetic lambda body for Optional.ifPresent in stopSleeping")
	@ModifyArg(method = {"lambda$stopSleeping$0", "startSleeping"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;setBedOccupied(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;Z)V"))
	private boolean setOccupiedState(boolean occupied, @Local BlockPos pos, @Local BlockState originalState) {
		return occupied || EntitySleepEvents.SET_BED_OCCUPATION_STATE.invoker().setBedOccupationState((LivingEntity) (Object) this, pos, originalState, occupied);
	}

	@Dynamic("lambda$stopSleeping$0: Synthetic lambda body for Optional.ifPresent in stopSleeping")
	@Redirect(method = "lambda$stopSleeping$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BedBlock;findStandUpPosition(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;F)Ljava/util/Optional;"))
	private Optional<Vec3> modifyWakeUpPosition(EntityType<?> type, CollisionGetter level, BlockPos pos, Direction direction, float yaw) {
		Optional<Vec3> original = Optional.empty();
		BlockState bedState = level.getBlockState(pos);

		if (bedState.getBlock() instanceof BedBlock) {
			original = BedBlock.findStandUpPosition(type, level, pos, direction, yaw);
		}

		Vec3 newPos = EntitySleepEvents.MODIFY_WAKE_UP_POSITION.invoker().modifyWakeUpPosition((LivingEntity) (Object) this, pos, bedState, original.orElse(null));
		return Optional.ofNullable(newPos);
	}
}
