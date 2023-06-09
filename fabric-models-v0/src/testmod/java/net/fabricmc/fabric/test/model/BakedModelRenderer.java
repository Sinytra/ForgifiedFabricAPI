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

package net.fabricmc.fabric.test.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.ArrayUtils;

public class BakedModelRenderer {
	private static final Direction[] CULL_FACES = ArrayUtils.add(Direction.values(), null);
	private static final RandomSource RANDOM = RandomSource.create();

	public static void renderBakedModel(BakedModel model, VertexConsumer vertices, PoseStack.Pose entry, int light) {
		for (Direction cullFace : CULL_FACES) {
			RANDOM.setSeed(42L);

			for (BakedQuad quad : model.getQuads(null, cullFace, RANDOM)) {
				vertices.putBulkData(entry, quad, 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
			}
		}
	}
}
