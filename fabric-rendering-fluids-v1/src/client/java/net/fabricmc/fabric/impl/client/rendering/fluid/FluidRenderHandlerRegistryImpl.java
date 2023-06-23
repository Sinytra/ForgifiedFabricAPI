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

import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.IdentityHashMap;
import java.util.Map;

public class FluidRenderHandlerRegistryImpl implements FluidRenderHandlerRegistry {
    /**
     * The water color of {@link net.minecraft.world.level.biome.Biomes#OCEAN}.
     */
    private static final int DEFAULT_WATER_COLOR = 0x3f76e4;
    private final Map<Fluid, FluidRenderHandler> handlers = new IdentityHashMap<>();
    private final Map<Fluid, FluidRenderHandler> modHandlers = new IdentityHashMap<>();
    private final Map<Block, Boolean> overlayBlocks = new IdentityHashMap<>();

    private LiquidBlockRenderer fluidRenderer;

    public FluidRenderHandlerRegistryImpl() {
    }

    @Override
    public FluidRenderHandler get(Fluid fluid) {
        return handlers.get(fluid);
    }

    public FluidRenderHandler getOverride(Fluid fluid) {
        return modHandlers.get(fluid);
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
        // Don't cache unreliable lookups where we can't get the exact info from forge
        return overlayBlocks.containsKey(block) ? overlayBlocks.get(block) : block instanceof HalfTransparentBlock || block instanceof LeavesBlock;
    }

    @Override
    public boolean isBlockTransparent(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return overlayBlocks.computeIfAbsent(state.getBlock(), k -> k.shouldDisplayFluidOverlay(state, level, pos, fluidState));
    }

    public void onFluidRendererReload(LiquidBlockRenderer renderer, TextureAtlasSprite[] waterSprites, TextureAtlasSprite[] lavaSprites, TextureAtlasSprite waterOverlay) {
        fluidRenderer = renderer;
        TextureAtlasSprite[] waterSpritesFull = {waterSprites[0], waterSprites[1], waterOverlay};
        FluidRenderHandler waterHandler = new FluidRenderHandler() {
            @Override
            public TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter view, BlockPos pos, FluidState state) {
                return waterSpritesFull;
            }

            @Override
            public int getFluidColor(BlockAndTintGetter view, BlockPos pos, FluidState state) {
                if (view != null && pos != null) {
                    return BiomeColors.getAverageWaterColor(view, pos);
                } else {
                    return DEFAULT_WATER_COLOR;
                }
            }
        };

        //noinspection Convert2Lambda
        FluidRenderHandler lavaHandler = new FluidRenderHandler() {
            @Override
            public TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter view, BlockPos pos, FluidState state) {
                return lavaSprites;
            }
        };

        register(Fluids.WATER, waterHandler);
        register(Fluids.FLOWING_WATER, waterHandler);
        register(Fluids.LAVA, lavaHandler);
        register(Fluids.FLOWING_LAVA, lavaHandler);
        handlers.putAll(modHandlers);

        TextureAtlas texture = Minecraft.getInstance()
                .getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS);

        for (FluidRenderHandler handler : handlers.values()) {
            handler.reloadTextures(texture);
        }
    }

    public void renderFluid(BlockPos pos, BlockAndTintGetter world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        fluidRenderer.tesselate(world, pos, vertexConsumer, blockState, fluidState);
    }
}
