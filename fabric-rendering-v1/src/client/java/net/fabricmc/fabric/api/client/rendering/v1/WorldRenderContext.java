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

package net.fabricmc.fabric.api.client.rendering.v1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Except as noted below, the properties exposed here match the parameters passed to
 * {@link LevelRenderer#renderLevel(PoseStack, float, long, boolean, Camera, GameRenderer, LightTexture, Matrix4f)}.
 */
public interface WorldRenderContext {
	/**
	 * The world renderer instance doing the rendering and invoking the event.
	 *
	 * @return WorldRenderer instance invoking the event
	 */
	LevelRenderer worldRenderer();

	PoseStack matrixStack();

	float tickDelta();

	long limitTime();

	boolean blockOutlines();

	Camera camera();

	GameRenderer gameRenderer();

	LightTexture lightmapTextureManager();

	Matrix4f projectionMatrix();

	/**
	 * Convenient access to {WorldRenderer.world}.
	 *
	 * @return world renderer's client world instance
	 */
	ClientLevel world();

	/**
	 * Convenient access to game performance profiler.
	 *
	 * @return the active profiler
	 */
	ProfilerFiller profiler();

	/**
	 * Test to know if "fabulous" graphics mode is enabled.
	 *
	 * <p>Use this for renders that need to render on top of all translucency to activate or deactivate different
	 * event handlers to get optimal depth testing results. When fabulous is off, it may be better to render
	 * during {@code WorldRenderLastCallback} after clouds and weather are drawn. Conversely, when fabulous mode is on,
	 * it may be better to draw during {@code WorldRenderPostTranslucentCallback}, before the fabulous mode composite
	 * shader runs, depending on which translucent buffer is being targeted.
	 *
	 * @return {@code true} when "fabulous" graphics mode is enabled.
	 */
	boolean advancedTranslucency();

	/**
	 * The {@code VertexConsumerProvider} instance being used by the world renderer for most non-terrain renders.
	 * Generally this will be better for most use cases because quads for the same layer can be buffered
	 * incrementally and then drawn all at once by the world renderer.
	 *
	 * <p>IMPORTANT - all vertex coordinates sent to consumers should be relative to the camera to
	 * be consistent with other quads emitted by the world renderer and other mods.  If this isn't
	 * possible, caller should use a separate "immediate" instance.
	 *
	 * <p>This property is {@code null} before {@link WorldRenderEvents#BEFORE_ENTITIES} and after
	 * {@link WorldRenderEvents#BEFORE_DEBUG_RENDER} because the consumer buffers are not available before or
	 * drawn after that in vanilla world rendering.  Renders that cannot draw in one of the supported events
	 * must be drawn directly to the frame buffer, preferably in {@link WorldRenderEvents#LAST} to avoid being
	 * overdrawn or cleared.
	 */
	@Nullable MultiBufferSource consumers();

	/**
	 * View frustum, after it is initialized. Will be {@code null} during
	 * {@link WorldRenderEvents#START}.
	 */
	@Nullable Frustum frustum();

	/**
	 * Used in {@code BLOCK_OUTLINE} to convey the parameters normally sent to
	 * {@code WorldRenderer.drawBlockOutline}.
	 */
	interface BlockOutlineContext {
		/**
		 * @deprecated Use {@link #consumers()} directly.
		 */
		@Deprecated
		VertexConsumer vertexConsumer();

		Entity entity();

		double cameraX();

		double cameraY();

		double cameraZ();

		BlockPos blockPos();

		BlockState blockState();
	}
}
