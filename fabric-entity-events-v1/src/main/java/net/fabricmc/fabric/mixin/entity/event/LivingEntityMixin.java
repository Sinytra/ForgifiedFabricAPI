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

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Shadow
    public abstract boolean isDeadOrDying();

    @Shadow
    public abstract Optional<BlockPos> getSleepingPos();
    
    @Unique
    private BlockState fabric_originalState;

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;wasKilled(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onEntityKilledOther(DamageSource source, CallbackInfo ci, Entity attacker) {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.invoker().afterKilledOtherEntity((ServerLevel) ((LivingEntity) (Object) this).level, attacker, (LivingEntity) (Object) this);
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
    private void notifyDeath(DamageSource source, CallbackInfo ci) {
        ServerLivingEntityEvents.AFTER_DEATH.invoker().afterDeath((LivingEntity) (Object) this, source);
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z", ordinal = 1))
    boolean beforeEntityKilled(LivingEntity livingEntity, DamageSource source, float amount) {
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

    @Inject(method = "getBedOrientation", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void onGetSleepingDirection(CallbackInfoReturnable<Direction> info, @Nullable BlockPos sleepingPos) {
        if (sleepingPos != null) {
            info.setReturnValue(EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.invoker().modifySleepDirection((LivingEntity) (Object) this, sleepingPos, info.getReturnValue()));
        }
    }

    // This is needed 1) so that the vanilla logic in wakeUp runs for modded beds and 2) for the injector below.
    // The injector is shared because lambda$stopSleeping$11 and sleep share much of the structure here.
    @Dynamic("lambda$stopSleeping$11: Synthetic lambda body for Optional.ifPresent in wakeUp")
    @ModifyVariable(method = {"lambda$stopSleeping$11", "m_260785_", "startSleeping"}, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), require = 2)
    private BlockState modifyBedForOccupiedState(BlockState state, BlockPos sleepingPos) {
        LivingEntity entity = (LivingEntity) (Object) this;
        InteractionResult result = EntitySleepEvents.ALLOW_BED.invoker().allowBed(entity, sleepingPos, state, state.isBed(entity.level, sleepingPos, entity));

        // If a valid bed, replace with vanilla red bed so that the BlockState#isBed and BlockState#getValue(FACING) check both succeed
        if (result.consumesAction()) {
            fabric_originalState = state;
            return Blocks.RED_BED.defaultBlockState();
        }
        fabric_originalState = null;
        return state;
    }

    // The injector is shared because method_18404 and sleep share much of the structure here.
    @Dynamic("lambda$stopSleeping$11: Synthetic lambda body for Optional.ifPresent in wakeUp")
    @Redirect(method = {"lambda$stopSleeping$11", "m_260785_", "startSleeping"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;setBedOccupied(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;Z)V"), require = 2)
    private void setOccupiedState(BlockState state, Level level, BlockPos pos, LivingEntity entity, boolean occupied) {
        // This might have been replaced by a red bed above, so we get it again.
        // Note that we *need* to replace it so the state.with(OCCUPIED, ...) call doesn't crash
        // when the bed doesn't have the property.
        BlockState originalState = fabric_originalState != null ? fabric_originalState : state;

        if (!EntitySleepEvents.SET_BED_OCCUPATION_STATE.invoker().setBedOccupationState(entity, pos, originalState, occupied)) {
            originalState.setBedOccupied(level, pos, entity, occupied);
        }
        
        fabric_originalState = null;
    }

    @Dynamic("lambda$stopSleeping$11: Synthetic lambda body for Optional.ifPresent in wakeUp")
    @Redirect(method = {"lambda$stopSleeping$11", "m_260785_"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BedBlock;findStandUpPosition(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;F)Ljava/util/Optional;"), require = 1)
    private Optional<Vec3> modifyWakeUpPosition(EntityType<?> entityType, CollisionGetter level, BlockPos pos, Direction direction, float yaw) {
        Optional<Vec3> original = Optional.empty();
        BlockState bedState = level.getBlockState(pos);

        if (bedState.isBed(level, pos, (LivingEntity) (Object) this)) {
            original = BedBlock.findStandUpPosition(entityType, level, pos, direction, yaw);
        }

        Vec3 newPos = EntitySleepEvents.MODIFY_WAKE_UP_POSITION.invoker().modifyWakeUpPosition((LivingEntity) (Object) this, pos, bedState, original.orElse(null));
        return Optional.ofNullable(newPos);
    }
}
