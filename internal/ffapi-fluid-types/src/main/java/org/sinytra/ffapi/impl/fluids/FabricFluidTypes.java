package org.sinytra.ffapi.impl.fluids;

import java.util.HashMap;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;

public class FabricFluidTypes {
	private static final Map<Fluid, FluidType> FLUID_TYPES = new HashMap<>();

	public static FluidType getFluidType(Fluid fluid) {
		return FLUID_TYPES.get(fluid);
	}

	public static void register(Fluid fluid, @Nullable FluidVariantAttributeHandler attributes) {
		ResourceKey<Fluid> key = fluid.builtInRegistryHolder().getKey();
		ResourceKey<FluidType> typeKey = ResourceKey.create(NeoForgeRegistries.Keys.FLUID_TYPES, key.identifier());

		FluidVariant variant = FluidVariant.of(fluid);
		FluidType.Properties properties = FluidType.Properties.create()
				.descriptionId(getDescriptionId(variant))
				.canPushEntity(false)
				.canSwim(false)
				.canDrown(false)
				.pathType(null)
				.adjacentPathType(null);

		FluidType type = new FabricFluidType(properties, variant, attributes);
		Registry.register(NeoForgeRegistries.FLUID_TYPES, typeKey, type);
		FLUID_TYPES.put(fluid, type);
	}

	private static class FabricFluidType extends FluidType {
		private final FluidVariant variant;
		@Nullable
		private final FluidVariantAttributeHandler handler;
		@Nullable
		private Pair<TagKey<Fluid>, FluidBehavior> behavior;

		public FabricFluidType(Properties properties, FluidVariant variant,
		                       @Nullable FluidVariantAttributeHandler handler) {
			super(properties);
			this.variant = variant;
			this.handler = handler;
		}

		private Pair<TagKey<Fluid>, FluidBehavior> getBehavior() {
			if (this.behavior == null) {
				this.behavior = FluidTypesImpl.getBehavior(this.variant.getFluid());
			}
			return this.behavior;
		}

		@Override
		public Component getDescription() {
			if (this.handler != null) {
				return this.handler.getName(this.variant);
			}
			return super.getDescription();
		}

		@Nullable
		@Override
		public SoundEvent getSound(SoundAction action) {
			if (this.handler != null) {
				if (action == SoundActions.BUCKET_FILL) {
					return this.handler.getFillSound(this.variant).orElse(null);
				} else if (action == SoundActions.BUCKET_EMPTY) {
					return this.handler.getEmptySound(this.variant).orElse(null);
				}
			}
			return super.getSound(action);
		}

		@Override
		public int getLightLevel() {
			if (this.handler != null) {
				return this.handler.getLightEmission(this.variant);
			}
			return super.getLightLevel();
		}

		@Override
		public int getTemperature() {
			if (this.handler != null) {
				return this.handler.getTemperature(this.variant);
			}
			return super.getTemperature();
		}

		@Override
		public int getViscosity() {
			if (this.handler != null) {
				return this.handler.getViscosity(this.variant, null);
			}
			return super.getViscosity();
		}

		@Override
		public int getDensity() {
			if (this.handler != null) {
				return this.handler.isLighterThanAir(this.variant) ? 0 : 1000;
			}
			return super.getDensity();
		}

		@Override
		public boolean move(LivingEntity entity, Vec3 movementVector, double gravity) {
			if (getBehavior() != null) {
				boolean isFalling = entity.getDeltaMovement().y <= 0;
				double oldY = entity.getY();
				getBehavior().getSecond().travelInFluid(getBehavior().getFirst(), entity, movementVector, gravity, isFalling, oldY);
				return true;
			}
			return super.move(entity, movementVector, gravity);
		}

		@Override
		public boolean canSwim(Entity entity) {
			if (getBehavior() != null) {
				return getBehavior().getSecond().canSwimInFluid(getBehavior().getFirst(), entity);
			}
			return super.canSwim(entity);
		}

		@Override
		public boolean canDrownIn(LivingEntity entity) {
			if (getBehavior() != null) {
				return getBehavior().getSecond().canDrownInFluid(getBehavior().getFirst(), entity);
			}
			return super.canDrownIn(entity);
		}

		@Override
		public boolean supportsBoating(AbstractBoat boat) {
			if (getBehavior() != null) {
				return getBehavior().getSecond().canSupportBoat(getBehavior().getFirst(), boat);
			}
			return super.supportsBoating(boat);
		}
	}

	@Nullable
	private static String getDescriptionId(FluidVariant variant) {
		Block fluidBlock = variant.getFluid().defaultFluidState().createLegacyBlock().getBlock();

		if (!variant.isBlank() && fluidBlock == Blocks.AIR) {
			// Some non-placeable fluids use air as their fluid block, in that case infer translation key from the fluid id.
			return Util.makeDescriptionId("block", BuiltInRegistries.FLUID.getKey(variant.getFluid()));
		} else {
			return fluidBlock.getDescriptionId();
		}
	}
}
