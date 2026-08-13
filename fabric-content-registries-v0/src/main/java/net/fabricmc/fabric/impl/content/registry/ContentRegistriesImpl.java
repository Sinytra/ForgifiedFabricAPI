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
import net.minecraft.world.level.material.Fluid;

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
		for (FluidType type : getFluidTypes(fluid)) {
			interaction.applyCurrentTo(type, entity, scale);
			return;
		}
	}

	public static double getFluidHeight(EntityFluidInteraction interaction, TagKey<Fluid> fluid) {
		for (FluidType type : getFluidTypes(fluid)) {
			return interaction.getFluidHeight(type);
		}
		return 0;
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
