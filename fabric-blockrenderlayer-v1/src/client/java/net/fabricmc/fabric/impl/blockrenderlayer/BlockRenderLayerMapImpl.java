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

package net.fabricmc.fabric.impl.blockrenderlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class BlockRenderLayerMapImpl implements BlockRenderLayerMap {
    public BlockRenderLayerMapImpl() {
    }

    @Override
    public void putBlock(Block block, RenderType renderLayer) {
        if (block == null) throw new IllegalArgumentException("Request to map null block to BlockRenderLayer");
        if (renderLayer == null) throw new IllegalArgumentException("Request to map block " + block.toString() + " to null BlockRenderLayer");

        synchronized (LOCK) {
            blockHandler.accept(block, renderLayer);
        }
    }

    @Override
    public void putBlocks(RenderType renderLayer, Block... blocks) {
        for (Block block : blocks) {
            putBlock(block, renderLayer);
        }
    }

    @Override
    public void putItem(Item item, RenderType renderLayer) {
        if (item == null) throw new IllegalArgumentException("Request to map null item to BlockRenderLayer");
        if (renderLayer == null) throw new IllegalArgumentException("Request to map item " + item.toString() + " to null BlockRenderLayer");

        synchronized (LOCK) {
            itemHandler.accept(item, renderLayer);
        }
    }

    @Override
    public void putItems(RenderType renderLayer, Item... items) {
        for (Item item : items) {
            putItem(item, renderLayer);
        }
    }

    @Override
    public void putFluid(Fluid fluid, RenderType renderLayer) {
        if (fluid == null) throw new IllegalArgumentException("Request to map null fluid to BlockRenderLayer");
        if (renderLayer == null) throw new IllegalArgumentException("Request to map fluid " + fluid.toString() + " to null BlockRenderLayer");

        synchronized (LOCK) {
            fluidHandler.accept(fluid, renderLayer);
        }
    }

    @Override
    public void putFluids(RenderType renderLayer, Fluid... fluids) {
        for (Fluid fluid : fluids) {
            putFluid(fluid, renderLayer);
        }
    }

    private static final Object LOCK = new Object();
    private static Map<Block, RenderType> blockRenderLayerMap = new HashMap<>();
    private static Map<Item, RenderType> itemRenderLayerMap = new HashMap<>();
    private static Map<Fluid, RenderType> fluidRenderLayerMap = new HashMap<>();

    //This consumers initially add to the maps above, and then are later set (when initialize is called) to insert straight into the target map.
    private static BiConsumer<Block, RenderType> blockHandler = (b, l) -> blockRenderLayerMap.put(b, l);
    private static BiConsumer<Item, RenderType> itemHandler = (i, l) -> itemRenderLayerMap.put(i, l);
    private static BiConsumer<Fluid, RenderType> fluidHandler = (f, b) -> fluidRenderLayerMap.put(f, b);

    public static void initialize(BiConsumer<Block, RenderType> blockHandlerIn, BiConsumer<Fluid, RenderType> fluidHandlerIn) {
        synchronized (LOCK) {
            //Done to handle backwards compat, in previous snapshots Items had their own map for render layers, now the BlockItem is used.
            BiConsumer<Item, RenderType> itemHandlerIn = (item, renderLayer) -> blockHandlerIn.accept(Block.byItem(item), renderLayer);

            //Add all the pre existing render layers
            blockRenderLayerMap.forEach(blockHandlerIn);
            itemRenderLayerMap.forEach(itemHandlerIn);
            fluidRenderLayerMap.forEach(fluidHandlerIn);

            //Set the handlers to directly accept later additions
            blockHandler = blockHandlerIn;
            itemHandler = itemHandlerIn;
            fluidHandler = fluidHandlerIn;
        }
    }
}
