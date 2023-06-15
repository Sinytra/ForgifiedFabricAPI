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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.test.rendering.TooltipComponentTestInit;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class DimensionalRenderingTest {
	private static final ResourceLocation END_SKY = new ResourceLocation("textures/block/dirt.png");

	private static void render(WorldRenderContext context) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, END_SKY);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();

		Matrix4f matrix4f = context.matrixStack().last().pose();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -100.0f).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -100.0f).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();

		bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, -100.0f).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -99.0f).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -99.0f).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, -100.0f).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();

		bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, 100.0f).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, 100.0f).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();

		bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, 101.0f).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, -100.0f).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, -100.0f).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, 100.0f).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();

		bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -100.0f).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, 100.0f).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, -100.0f).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();

		bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, -100.0f).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, 100.0f).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -100.0f).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();
		tessellator.end();

		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
	}

	public static void onInitializeClient() {
		DimensionRenderingRegistry.registerSkyRenderer(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(TooltipComponentTestInit.MODID, "void")), DimensionalRenderingTest::render);
		DimensionRenderingRegistry.registerDimensionEffects(new ResourceLocation(TooltipComponentTestInit.MODID, "void"), new DimensionSpecialEffects.EndEffects());
	}
}
