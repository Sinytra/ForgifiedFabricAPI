package org.sinytra.ffapi.impl.fluids;

import com.mojang.datafixers.util.Pair;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.mixin.transfer.registry.BaseMappedRegistryAccessor;
import net.fabricmc.fabric.mixin.transfer.registry.MappedRegistryAccessor;

@Mod(FluidTypesImpl.MODID)
public class FluidTypesImpl {
	public static final String MODID = "ffapi_fluid_types";

	public FluidTypesImpl(IEventBus bus) {
		bus.addListener(EventPriority.LOWEST, FluidTypesImpl::setupFluidTypes);
	}

	private static void setupFluidTypes(FMLCommonSetupEvent event) {
		boolean frozen = ((MappedRegistryAccessor) NeoForgeRegistries.FLUID_TYPES).getFrozen();
		if (frozen) {
			((BaseMappedRegistryAccessor) NeoForgeRegistries.FLUID_TYPES).invokeUnfreeze(false);
		}

		for (Fluid fluid : BuiltInRegistries.FLUID) {
			if (definesCustomFluidType(fluid)) {
				continue;
			}

			FluidVariantAttributeHandler attributes = FluidVariantAttributes.getHandler(fluid);

			if (attributes != null) {
				FabricFluidTypes.register(fluid, attributes);
			}
		}

		if (frozen) {
			NeoForgeRegistries.FLUID_TYPES.freeze();
		}
	}

	@Nullable
	public static Pair<TagKey<Fluid>, FluidBehavior> getBehavior(Fluid fluid) {
		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistry.getCustomInteractableFluids()) {
			if (fluid.is(tagKey)) {
				FluidBehavior behavior = EntityFluidInteractionRegistry.getFluidBehavior(tagKey);
				return Pair.of(tagKey, behavior);
			}
		}
		return null;
	}

	private static boolean definesCustomFluidType(Fluid fluid) {
		try {
			fluid.getFluidType();
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}
}
