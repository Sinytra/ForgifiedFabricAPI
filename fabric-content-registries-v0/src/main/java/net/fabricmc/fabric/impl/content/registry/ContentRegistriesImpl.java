package net.fabricmc.fabric.impl.content.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.fluids.FluidType;
import org.sinytra.fabric.content_registries.generated.GeneratedEntryPoint;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.impl.content.registry.fluid.EntityFluidInteractionRegistryImpl;
import net.fabricmc.fabric.mixin.content.registry.fluid.EntityFluidInteractionAccessor;
import net.fabricmc.fabric.mixin.content.registry.fluid.EntityFluidInteractionTrackerAccessor;

@Mod(GeneratedEntryPoint.MOD_ID)
public class ContentRegistriesImpl {
	private static final Map<TagKey<Fluid>, Collection<FluidType>> FLUID_TYPE_CACHE = new HashMap<>();

	public static boolean isInFluid(EntityFluidInteraction interaction, TagKey<Fluid> tagKey) {
		return getFluidTypes(tagKey).stream().anyMatch(interaction::isInFluid);
	}

	public static boolean isEyeInFluid(EntityFluidInteraction interaction, TagKey<Fluid> tagKey) {
		return getFluidTypes(tagKey).stream().anyMatch(interaction::isEyeInFluid);
	}

	public static void applyCurrentTo(EntityFluidInteraction interaction, TagKey<Fluid> fluid, Entity entity, double scale) {
		Map<FluidType, Object> trackers = ((EntityFluidInteractionAccessor) interaction).getCurrentAccumulators();
		Vec3 current = Vec3.ZERO;
		int allCurrentCount = 0;

		for (FluidType type : getFluidTypes(fluid)) {
			if (trackers.get(type) instanceof EntityFluidInteractionTrackerAccessor tracker) {
				current = current.add(tracker.getAccumulatedCurrent());
				allCurrentCount += tracker.getCurrentCount();
			}
		}

		// From EntityFluidInteraction.Tracker#applyCurrentTo
		if (allCurrentCount != 0 && !(current.lengthSqr() < 1.0E-5F)) {
			Vec3 impulse;

			if (!(entity instanceof Player)) {
				impulse = current.normalize();
			} else {
				impulse = current.scale(1.0 / allCurrentCount);
			}

			Vec3 oldMovement = entity.getDeltaMovement();
			impulse = impulse.scale(scale);

			if (Math.abs(oldMovement.x) < 0.003 && Math.abs(oldMovement.z) < 0.003 && impulse.length() < 0.0045000000000000005) {
				impulse = impulse.normalize().scale(0.0045000000000000005);
			}

			entity.addDeltaMovement(impulse);
		}
	}

	public static double getFluidHeight(EntityFluidInteraction interaction, TagKey<Fluid> fluid) {
		double height = 0;

		for (FluidType type : getFluidTypes(fluid)) {
			height = Math.max(height, interaction.getFluidHeight(type));
		}

		return height;
	}

	public static boolean isCustomFluidType(FluidType type) {
		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			if (getFluidTypes(tagKey).contains(type)) {
				return true;
			}
		}
		return false;
	}

	public static Collection<FluidType> getFluidTypes(TagKey<Fluid> tagKey) {
		return FLUID_TYPE_CACHE.computeIfAbsent(tagKey, ContentRegistriesImpl::computeFluidTypes);
	}

	private static Collection<FluidType> computeFluidTypes(TagKey<Fluid> tagKey) {
		return StreamSupport.stream(BuiltInRegistries.FLUID.getTagOrEmpty(tagKey).spliterator(), false)
				.map(f -> f.value().getFluidType())
				.distinct()
				.toList();
	}
}
