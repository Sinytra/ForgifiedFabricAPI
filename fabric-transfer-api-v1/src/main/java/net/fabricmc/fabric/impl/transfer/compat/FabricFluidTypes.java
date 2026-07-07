package net.fabricmc.fabric.impl.transfer.compat;

import java.util.HashMap;
import java.util.Map;

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
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.mixin.transfer.registry.BaseMappedRegistryAccessor;
import net.fabricmc.fabric.mixin.transfer.registry.MappedRegistryAccessor;

public class FabricFluidTypes {
	private static final Map<Fluid, FluidType> FLUID_TYPES = new HashMap<>();

	public static FluidType getFluidType(Fluid fluid) {
		return FLUID_TYPES.get(fluid);
	}

	public static void register(Fluid fluid, FluidVariantAttributeHandler handler) {
		boolean frozen = ((MappedRegistryAccessor) NeoForgeRegistries.FLUID_TYPES).getFrozen();
		if (frozen) {
			((BaseMappedRegistryAccessor) NeoForgeRegistries.FLUID_TYPES).invokeUnfreeze(false);
		}

		ResourceKey<Fluid> key = fluid.builtInRegistryHolder().getKey();
		ResourceKey<FluidType> typeKey = ResourceKey.create(NeoForgeRegistries.Keys.FLUID_TYPES, key.identifier());

		FluidVariant variant = FluidVariant.of(fluid);
		FluidType.Properties properties = FluidType.Properties.create()
				.descriptionId(getDescriptionId(variant))
				.motionScale(1D)
				.canPushEntity(false)
				.canSwim(false)
				.canDrown(false)
				.fallDistanceModifier(1F)
				.pathType(null)
				.adjacentPathType(null);

		FluidType type = new FabricFluidType(properties, variant, handler);
		Registry.register(NeoForgeRegistries.FLUID_TYPES, typeKey, type);
		FLUID_TYPES.put(fluid, type);

		if (frozen) {
			NeoForgeRegistries.FLUID_TYPES.freeze();
		}
	}

	private static class FabricFluidType extends FluidType {
		private final FluidVariant variant;
		private final FluidVariantAttributeHandler handler;

		public FabricFluidType(Properties properties, FluidVariant variant, FluidVariantAttributeHandler handler) {
			super(properties);
			this.variant = variant;
			this.handler = handler;
		}

		@Override
		public Component getDescription() {
			return this.handler.getName(this.variant);
		}

		@Nullable
		@Override
		public SoundEvent getSound(SoundAction action) {
			if (action == SoundActions.BUCKET_FILL) {
				return this.handler.getFillSound(this.variant).orElse(null);
			} else if (action == SoundActions.BUCKET_EMPTY) {
				return this.handler.getEmptySound(this.variant).orElse(null);
			}
			return super.getSound(action);
		}

		@Override
		public int getLightLevel() {
			return this.handler.getLightEmission(this.variant);
		}

		@Override
		public int getTemperature() {
			return this.handler.getTemperature(this.variant);
		}

		@Override
		public int getViscosity() {
			return this.handler.getViscosity(this.variant, null);
		}

		@Override
		public int getDensity() {
			return this.handler.isLighterThanAir(this.variant) ? 0 : 1000;
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
