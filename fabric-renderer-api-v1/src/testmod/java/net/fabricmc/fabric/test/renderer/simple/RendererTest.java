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

package net.fabricmc.fabric.test.renderer.simple;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.test.renderer.simple.client.RendererClientTest;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

/**
 * A simple testmod that renders a simple block rendered using the fabric renderer api.
 * The block that is rendered is a simple frame that another block is rendered in.
 * Blocks that provide a block entity cannot be placed inside the frame.
 *
 * <p>There are no fancy shaders or glow that is provided by this renderer test.
 */
@Mod(RendererTest.MODID)
public final class RendererTest {
	public static final String MODID = "fabric_renderer_api_v1_testmod";
	public static final FrameBlock[] FRAMES = new FrameBlock[]{
			new FrameBlock(id("frame")),
			new FrameBlock(id("frame_multipart")),
			new FrameBlock(id("frame_weighted")),
	};
	public static final BlockEntityType<FrameBlockEntity> FRAME_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(FrameBlockEntity::new, FRAMES).build(null);

	public static final ResourceLocation PILLAR_ID = id("pillar");
	public static final Block PILLAR = new Block(FabricBlockSettings.of(Material.STONE));

	public RendererTest() {
		if (FMLLoader.getDist() == Dist.CLIENT) {
			RendererClientTest.onInitializeClient();
		}
		
		for (FrameBlock frameBlock : FRAMES) {
			Registry.register(Registries.BLOCK, frameBlock.id, frameBlock);
			Registry.register(Registries.ITEM, frameBlock.id, new BlockItem(frameBlock, new Item.Properties()));
		}

		// To anyone testing this: pillars are supposed to connect vertically with each other.
		// Additionally, they should also connect vertically to frame blocks containing a pillar.
		// (The frame block will not change, but adjacent pillars should adjust their textures).
		Registry.register(Registries.BLOCK, PILLAR_ID, PILLAR);
		Registry.register(Registries.ITEM, PILLAR_ID, new BlockItem(PILLAR, new Item.Properties()));

		Registry.register(Registries.BLOCK_ENTITY_TYPE, id("frame"), FRAME_BLOCK_ENTITY);
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
}
