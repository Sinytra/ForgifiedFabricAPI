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

package net.fabricmc.fabric.mixin.entity.event.effect;

import java.util.Collection;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.fabricmc.fabric.impl.entity.event.effect.MobEffectUtil;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	private LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
			method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"
			)
	)
	private void beforeAddEffect(MobEffectInstance effectInstance, Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (this.isClient()) {
			return;
		}

		ServerMobEffectEvents.BEFORE_ADD.invoker().beforeAdd(effectInstance, this.fabric$self(), MobEffectUtil.getCommandContext());
	}

	@Inject(
			method = "forceAddEffect",
			at = @At(
					value = "INVOKE",
					target = "Lnet/neoforged/neoforge/common/CommonHooks;canMobEffectBeApplied(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
					shift = At.Shift.AFTER
			)
	)
	private void beforeForceAddEffect(MobEffectInstance effectInstance, Entity entity, CallbackInfo ci) {
		if (this.isClient()) {
			return;
		}

		ServerMobEffectEvents.BEFORE_ADD.invoker().beforeAdd(effectInstance, this.fabric$self(), MobEffectUtil.getCommandContext());
	}

	@Inject(
			method = "onEffectAdded",
			at = @At("RETURN")
	)
	private void afterAddEffect(MobEffectInstance effectInstance, Entity entity, CallbackInfo ci) {
		if (this.isClient()) {
			return;
		}

		ServerMobEffectEvents.AFTER_ADD.invoker().afterAdd(effectInstance, this.fabric$self(), MobEffectUtil.getCommandContext());
	}

	@Inject(
			method = "removeEffect",
			at = @At("HEAD")
	)
	private void beforeRemoveEffect(Holder<MobEffect> holder, CallbackInfoReturnable<Boolean> cir) {
		if (this.isClient()) {
			return;
		}

		MobEffectInstance effectInstance = this.fabric$self().getEffect(holder);

		if (effectInstance == null) {
			return;
		}

		ServerMobEffectEvents.BEFORE_REMOVE.invoker()
				.beforeRemove(effectInstance, (LivingEntity) (Object) this, MobEffectUtil.getCommandContext());
	}

	@Inject(
			method = "tickEffects",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Iterator;remove()V"
			)
	)
	private void beforeExpireRemoveEffect(CallbackInfo ci, @Local(name = "effect") MobEffectInstance effect) {
		if (this.isClient()) {
			return;
		}

		ServerMobEffectEvents.BEFORE_REMOVE.invoker()
				.beforeRemove(effect, this.fabric$self(), MobEffectUtil.getCommandContext());
	}

	@Inject(
			method = "removeAllEffects",
			at = @At(
					value = "NEW",
					target = "java/util/HashMap"
			)
	)
	private void beforeRemoveAllEffects(CallbackInfoReturnable<Boolean> cir) {
		if (this.isClient()) {
			return;
		}

		for (MobEffectInstance effectInstance : (this.fabric$self()).getActiveEffects()) {
			ServerMobEffectEvents.BEFORE_REMOVE.invoker()
					.beforeRemove(effectInstance, this.fabric$self(), MobEffectUtil.getCommandContext());
		}
	}

	@Inject(
			method = "onEffectsRemoved",
			at = @At("RETURN")
	)
	private void afterRemoveEffect(Collection<MobEffectInstance> collection, CallbackInfo ci) {
		if (this.isClient()) {
			return;
		}

		for (MobEffectInstance effectInstance : collection) {
			ServerMobEffectEvents.AFTER_REMOVE.invoker()
					.afterRemove(effectInstance, this.fabric$self(), MobEffectUtil.getCommandContext());
		}
	}

	@Unique
	private boolean isClient() {
		return this.level().isClientSide();
	}

	@Unique
	private LivingEntity fabric$self() {
		return (LivingEntity) (Object) this;
	}
}
