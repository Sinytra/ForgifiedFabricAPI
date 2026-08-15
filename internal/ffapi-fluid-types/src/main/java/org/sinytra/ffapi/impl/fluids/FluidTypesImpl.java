package org.sinytra.ffapi.impl.fluids;

import java.util.Map.Entry;

import com.mojang.datafixers.util.Pair;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.mixin.transfer.registry.BaseMappedRegistryAccessor;
import net.fabricmc.fabric.mixin.transfer.registry.MappedRegistryAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;

@Mod(FluidTypesImpl.MODID)
public class FluidTypesImpl {
	public static final String MODID = "ffapi_fluid_types";

	private static final String POLYFILL_FLUID_TYPES = "sinytra:polyfill_fluid_types";

	public FluidTypesImpl(IEventBus bus) {
		bus.addListener(EventPriority.LOWEST, FluidTypesImpl::setupFluidTypes);
	}

	private static void setupFluidTypes(FMLCommonSetupEvent event) {
		boolean frozen = ((MappedRegistryAccessor) NeoForgeRegistries.FLUID_TYPES).getFrozen();
		if (frozen) {
			((BaseMappedRegistryAccessor) NeoForgeRegistries.FLUID_TYPES).invokeUnfreeze(false);
		}

		registerPolyfillFluidAttributeHandlers();

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

	private static void registerPolyfillFluidAttributeHandlers() {
		for (Entry<ResourceKey<Fluid>, Fluid> entry : BuiltInRegistries.FLUID.entrySet()) {
			ResourceKey<Fluid> key = entry.getKey();
			Fluid fluid = entry.getValue();

			boolean polyfill = FabricLoader.getInstance().getModContainer(key.identifier().getNamespace())
					.map(c -> c.getMetadata().getCustomValue(POLYFILL_FLUID_TYPES))
					.map(CustomValue::getAsBoolean)
					.orElse(false);
			if (!polyfill) {
				continue;
			}

			if (FluidVariantAttributes.getHandler(fluid) != null || definesCustomFluidType(fluid)) {
				continue;
			}

			FluidVariantAttributes.register(fluid, FluidVariantAttributes.getHandlerOrDefault(fluid));
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
