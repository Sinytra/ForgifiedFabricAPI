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

package net.fabricmc.fabric.impl.client.rendering.fluid;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class FluidRenderHandlerRegistryImpl implements FluidRenderHandlerRegistry {
	private final Map<Fluid, FluidRenderHandler> handlers = new IdentityHashMap<>();
	private final Map<Fluid, FluidRenderHandler> modHandlers = new IdentityHashMap<>();
	private final ConcurrentMap<Block, Boolean> overlayBlocks = new ConcurrentHashMap<>();

	{
		handlers.put(Fluids.WATER, WaterRenderHandler.INSTANCE);
		handlers.put(Fluids.FLOWING_WATER, WaterRenderHandler.INSTANCE);
		handlers.put(Fluids.LAVA, LavaRenderHandler.INSTANCE);
		handlers.put(Fluids.FLOWING_LAVA, LavaRenderHandler.INSTANCE);
	}

	public FluidRenderHandlerRegistryImpl() {
	}

	@Override
	@Nullable
	public FluidRenderHandler get(Fluid fluid) {
		return handlers.get(fluid);
	}

	@Override
	@Nullable
	public FluidRenderHandler getOverride(Fluid fluid) {
		return modHandlers.get(fluid);
	}

	public void registerHandlerOnly(Fluid fluid, FluidRenderHandler renderer) {
		handlers.put(fluid, renderer);
	}

	@Override
	public void register(Fluid fluid, FluidRenderHandler renderer) {
		handlers.put(fluid, renderer);
		modHandlers.put(fluid, renderer);
	}

	@Override
	public void setBlockTransparency(Block block, boolean transparent) {
		overlayBlocks.put(block, transparent);
	}

	@Override
	public boolean isBlockTransparent(Block block) {
		if (overlayBlocks.containsKey(block)) {
			return overlayBlocks.get(block);
		}
		return block instanceof HalfTransparentBlock || block instanceof LeavesBlock;
	}

	@Override
	public boolean isBlockTransparent(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        Block block = state.getBlock();
        Boolean existing = overlayBlocks.get(block);
        if (existing == null) {
            existing = block.shouldDisplayFluidOverlay(state, level, pos, fluidState);
            overlayBlocks.put(block, existing);
        }
		return existing;
	}

	public void onFluidRendererReload(LiquidBlockRenderer renderer, TextureAtlasSprite[] waterSprites, TextureAtlasSprite[] lavaSprites, TextureAtlasSprite waterOverlay) {
		FluidRenderingImpl.setVanillaRenderer(renderer);

		WaterRenderHandler.INSTANCE.updateSprites(waterSprites, waterOverlay);
		LavaRenderHandler.INSTANCE.updateSprites(lavaSprites);

		TextureAtlas texture = Minecraft.getInstance()
				.getModelManager()
				.getAtlas(InventoryMenu.BLOCK_ATLAS);

		for (FluidRenderHandler handler : handlers.values()) {
			handler.reloadTextures(texture);
		}
	}

	private static class WaterRenderHandler implements FluidRenderHandler {
		public static final WaterRenderHandler INSTANCE = new WaterRenderHandler();

		/**
		 * The water color of {@link Biomes#OCEAN}.
		 */
		private static final int DEFAULT_WATER_COLOR = 0x3f76e4;

		private final TextureAtlasSprite[] sprites = new TextureAtlasSprite[3];

		@Override
		public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
			return sprites;
		}

		@Override
		public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
			if (view != null && pos != null) {
				return BiomeColors.getAverageWaterColor(view, pos);
			} else {
				return DEFAULT_WATER_COLOR;
			}
		}

		public void updateSprites(TextureAtlasSprite[] waterSprites, TextureAtlasSprite waterOverlay) {
			sprites[0] = waterSprites[0];
			sprites[1] = waterSprites[1];
			sprites[2] = waterOverlay;
		}
	}

	private static class LavaRenderHandler implements FluidRenderHandler {
		public static final LavaRenderHandler INSTANCE = new LavaRenderHandler();

		private TextureAtlasSprite[] sprites;

		@Override
		public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
			return sprites;
		}

		public void updateSprites(TextureAtlasSprite[] lavaSprites) {
			sprites = lavaSprites;
		}
	}
}
