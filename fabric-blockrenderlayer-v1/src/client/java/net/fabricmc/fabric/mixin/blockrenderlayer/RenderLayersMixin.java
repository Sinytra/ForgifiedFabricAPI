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

package net.fabricmc.fabric.mixin.blockrenderlayer;

import net.fabricmc.fabric.impl.blockrenderlayer.ExtendedChunkRenderTypeSet;

import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayers;

import net.minecraft.registry.entry.RegistryEntry;

import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(RenderLayers.class)
public class RenderLayersMixin {
	@Shadow
	@Final
	private static Map<RegistryEntry.Reference<Block>, ChunkRenderTypeSet> BLOCK_RENDER_TYPES;

	/**
	 * @author embeddedt
	 * @reason Make getBlockLayer behave correctly (so that it can be used by Fabric mods) as long as the block has
	 * only a single render type
	 */
	@Redirect(method = {"getBlockLayer", "getMovingBlockLayer" }, at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
	private static Object getRenderLayerViaForge(Map instance, Object block) {
		ChunkRenderTypeSet renderTypeSet = BLOCK_RENDER_TYPES.get(ForgeRegistries.BLOCKS.getDelegateOrThrow((Block)block));
		return ((ExtendedChunkRenderTypeSet)(Object)renderTypeSet).sinytra$firstLayer();
	}
}
