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

package net.fabricmc.fabric.mixin.content.registry.fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.impl.content.registry.ContentRegistriesImpl;

@Mixin(EntityFluidInteraction.class)
public class EntityFluidInteractionMixin {
	@Inject(method = "applyCurrentTo(Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/entity/Entity;D)V", at = @At("HEAD"), cancellable = true)
	private void applyCustomFluidCurrent(TagKey<Fluid> fluid, Entity entity, double scale, CallbackInfo ci) {
		if (isCustomTag(fluid)) {
			ContentRegistriesImpl.applyCurrentTo((EntityFluidInteraction) (Object) this, fluid, entity, scale);
			ci.cancel();
		}
	}

	@Inject(method = "getFluidHeight(Lnet/minecraft/tags/TagKey;)D", at = @At("HEAD"), cancellable = true)
	private void getCustomFluidHeight(TagKey<Fluid> fluid, CallbackInfoReturnable<Double> cir) {
		if (isCustomTag(fluid)) {
			cir.setReturnValue(ContentRegistriesImpl.getFluidHeight((EntityFluidInteraction) (Object) this, fluid));
		}
	}

	@Inject(method = "isInFluid(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
	private void isInCustomFluid(TagKey<Fluid> fluid, CallbackInfoReturnable<Boolean> cir) {
		if (isCustomTag(fluid)) {
			cir.setReturnValue(ContentRegistriesImpl.isInFluid((EntityFluidInteraction) (Object) this, fluid));
		}
	}

	@Inject(method = "isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
	private void isEyeInCustomFluid(TagKey<Fluid> fluid, CallbackInfoReturnable<Boolean> cir) {
		if (isCustomTag(fluid)) {
			cir.setReturnValue(ContentRegistriesImpl.isEyeInFluid((EntityFluidInteraction) (Object) this, fluid));
		}
	}

	@Unique
	private static boolean isCustomTag(TagKey<Fluid> fluid) {
		return fluid != FluidTags.WATER && fluid != FluidTags.LAVA;
	}
}
