package net.fabricmc.fabric.impl.client.rendering.fluid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;

public final class FluidRendererCompat {

	public static void onClientSetup(FMLClientSetupEvent event) {
		// Register forge handlers only to the "handlers" map and not "modHandlers"
		// This allows fabric mods to access render handlers for forge mods' fluids without them being
		// used for rendering fluids, as that should remain handled by forge
		Map<FluidType, FluidRenderHandler> forgeHandlers = new HashMap<>();
		for (Map.Entry<RegistryKey<Fluid>, Fluid> entry : ForgeRegistries.FLUIDS.getEntries()) {
			Fluid fluid = entry.getValue();
			if (fluid != Fluids.EMPTY && IClientFluidTypeExtensions.of(fluid) == IClientFluidTypeExtensions.DEFAULT) {
				FluidRenderHandler handler = forgeHandlers.computeIfAbsent(fluid.getFluidType(), ForgeFluidRenderHandler::new);
				((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).registerHandlerOnly(fluid, handler);
			}
		}
	}

	private record ForgeFluidRenderHandler(FluidType fluidType) implements FluidRenderHandler {
		@Override
		public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
			Sprite[] forgeSprites = ForgeHooksClient.getFluidSprites(view, pos, state);
			return forgeSprites[2] == null ? Arrays.copyOfRange(forgeSprites, 0, 2) : forgeSprites;
		}

		@Override
		public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
			int color = IClientFluidTypeExtensions.of(this.fluidType).getTintColor(state, view, pos);
			return 0x00FFFFFF & color;
		}
	}

	private FluidRendererCompat() {
	}
}
