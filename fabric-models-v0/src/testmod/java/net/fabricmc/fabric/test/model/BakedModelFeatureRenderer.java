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
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.function.Supplier;

public class BakedModelFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private Supplier<BakedModel> modelSupplier;

	public BakedModelFeatureRenderer(RenderLayerParent<T, M> context, Supplier<BakedModel> modelSupplier) {
		super(context);
		this.modelSupplier = modelSupplier;
	}

	@Override
	public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		BakedModel model = modelSupplier.get();
		VertexConsumer vertices = vertexConsumers.getBuffer(Sheets.cutoutBlockSheet());
		matrices.pushPose();
		//matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(headYaw));
		//matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(headPitch));
		matrices.mulPose(new Quaternionf(new AxisAngle4f(animationProgress * 0.07F, 0, 1, 0)));
		matrices.scale(-0.75F, -0.75F, 0.75F);
		float aboveHead = (float) (Math.sin(animationProgress * 0.08F)) * 0.5F + 0.5F;
		matrices.translate(-0.5F, 0.75F + aboveHead, -0.5F);
		BakedModelRenderer.renderBakedModel(model, vertices, matrices.last(), light);
		matrices.popPose();
	}
}
