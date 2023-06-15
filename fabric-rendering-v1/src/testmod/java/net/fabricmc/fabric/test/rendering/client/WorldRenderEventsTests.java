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

package net.fabricmc.fabric.test.rendering.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class WorldRenderEventsTests {
	private static boolean onBlockOutline(WorldRenderContext wrc, WorldRenderContext.BlockOutlineContext blockOutlineContext) {
		if (blockOutlineContext.blockState().is(Blocks.DIAMOND_BLOCK)) {
			wrc.matrixStack().pushPose();
			Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			BlockPos pos = blockOutlineContext.blockPos();
			double x = pos.getX() - cameraPos.x;
			double y = pos.getY() - cameraPos.y;
			double z = pos.getZ() - cameraPos.z;
			wrc.matrixStack().translate(x+0.25, y+0.25+1, z+0.25);
			wrc.matrixStack().scale(0.5f, 0.5f, 0.5f);

			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
					Blocks.DIAMOND_BLOCK.defaultBlockState(),
					wrc.matrixStack(), wrc.consumers(), 15728880, OverlayTexture.NO_OVERLAY);

			wrc.matrixStack().popPose();
		}

		return true;
	}

	// Renders a diamond block above diamond blocks when they are looked at.
	public static void onInitializeClient() {
		WorldRenderEvents.BLOCK_OUTLINE.register(WorldRenderEventsTests::onBlockOutline);
	}
}
