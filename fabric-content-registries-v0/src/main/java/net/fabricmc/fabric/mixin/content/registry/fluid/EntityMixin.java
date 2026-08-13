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

import java.util.HashSet;
import java.util.Set;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.registry.fluid.EntityFluidExtension;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
import net.fabricmc.fabric.impl.content.registry.ContentRegistriesImpl;
import net.fabricmc.fabric.impl.content.registry.fluid.EntityFluidInteractionRegistryImpl;
import net.fabricmc.fabric.impl.content.registry.fluid.InternalEntityFluidExtension;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityFluidExtension, InternalEntityFluidExtension {
	@Shadow
	@Final
	private EntityFluidInteraction fluidInteraction;

	@Shadow
	public abstract boolean isPushedByFluid();

	@Shadow
	protected boolean firstTick;

	@Shadow
	public abstract boolean isInWater();

	@Shadow
	public abstract boolean isInLava();

	@Unique
	private final Set<TagKey<Fluid>> wasTouchingCustomFluid = new HashSet<>();

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityFluidInteraction;<init>(Ljava/util/Set;)V"))
	private Set<TagKey<Fluid>> addCustomTags(Set<TagKey<Fluid>> fluids) {
		var result = new HashSet<>(fluids);
		result.addAll(EntityFluidInteractionRegistryImpl.getTrackedFluids());
		return result;
	}

	@ModifyReturnValue(method = "isInLiquid", at = @At("RETURN"))
	private boolean checkForCustomFluids(boolean original) {
		return original || !this.wasTouchingCustomFluid.isEmpty();
	}

	@ModifyReturnValue(method = "updateFluidInteraction", at = @At("RETURN"))
	private boolean handleCustomFluidInteractionUpdates(boolean hasInteracted) {
		final boolean isPushedByFluid = this.isPushedByFluid();

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = ContentRegistriesImpl.isInFluid(this.fluidInteraction, tagKey);
			boolean wasInFluid = this.wasTouchingCustomFluid.contains(tagKey);

			if (inFluid) {
				FluidBehavior fluidBehavior = EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey);

				if (!wasInFluid) {
					fluidBehavior.onFluidEntered(tagKey, (Entity) (Object) this, this.firstTick);
					this.wasTouchingCustomFluid.add(tagKey);
				}

				hasInteracted = true;
				fluidBehavior.handleFluidInteractionUpdate(tagKey, (Entity) (Object) this, this.fluidInteraction, isPushedByFluid);
			} else if (wasInFluid) {
				this.wasTouchingCustomFluid.remove(tagKey);
				EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey).onFluidExited(tagKey, (Entity) (Object) this);
			}
		}

		return hasInteracted;
	}

	@Override
	public boolean isInFluid(TagKey<Fluid> fluid) {
		if (fluid == FluidTags.WATER) {
			return this.isInWater();
		} else if (fluid == FluidTags.LAVA) {
			return this.isInLava();
		}

		return this.wasTouchingCustomFluid.contains(fluid);
	}

	@Override
	public Set<TagKey<Fluid>> fabric_api$getTouchedCustomFluids() {
		return this.wasTouchingCustomFluid;
	}
}
