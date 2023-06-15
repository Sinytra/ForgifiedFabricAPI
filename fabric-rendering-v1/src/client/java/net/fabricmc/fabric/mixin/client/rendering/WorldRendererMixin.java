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

package net.fabricmc.fabric.mixin.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
	@Final
	@Shadow
	private RenderBuffers renderBuffers;
	@Shadow private ClientLevel level;
	@Shadow private PostChain transparencyChain;
	@Final
	@Shadow
	private Minecraft minecraft;
	@Unique private final WorldRenderContextImpl context = new WorldRenderContextImpl();
	@Unique private boolean didRenderParticles;

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void beforeRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		context.prepare((LevelRenderer) (Object) this, matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f, renderBuffers.bufferSource(), level.getProfiler(), transparencyChain != null, level);
		WorldRenderEvents.START.invoker().onStart(context);
		didRenderParticles = false;
	}

	@Inject(method = "setupRender", at = @At("RETURN"))
	private void afterTerrainSetup(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
		context.setFrustum(frustum);
		WorldRenderEvents.AFTER_SETUP.invoker().afterSetup(context);
	}

	@Inject(
			method = "renderLevel",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLorg/joml/Matrix4f;)V",
				ordinal = 2,
				shift = Shift.AFTER
			)
	)
	private void afterTerrainSolid(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_ENTITIES.invoker().beforeEntities(context);
	}

	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=blockentities", ordinal = 0))
	private void afterEntities(CallbackInfo ci) {
		WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(context);
	}

	@Inject(
			method = "renderLevel",
			at = @At(
				value = "FIELD",
				target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;",
				shift = At.Shift.AFTER,
				ordinal = 1
			)
	)
	private void beforeRenderOutline(CallbackInfo ci) {
		context.renderBlockOutline = WorldRenderEvents.BEFORE_BLOCK_OUTLINE.invoker().beforeBlockOutline(context, minecraft.hitResult);
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "renderHitOutline", at = @At("HEAD"), cancellable = true)
	private void onDrawBlockOutline(PoseStack matrixStack, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		if (!context.renderBlockOutline) {
			// Was cancelled before we got here, so do not
			// fire the BLOCK_OUTLINE event per contract of the API.
			ci.cancel();
		} else {
			context.prepareBlockOutline(entity, cameraX, cameraY, cameraZ, blockPos, blockState);

			if (!WorldRenderEvents.BLOCK_OUTLINE.invoker().onBlockOutline(context, context)) {
				ci.cancel();
			}

			// The immediate mode VertexConsumers use a shared buffer, so we have to make sure that the immediate mode VCP
			// can accept block outline lines rendered to the existing vertexConsumer by the vanilla block overlay.
			context.consumers().getBuffer(RenderType.lines());
		}
	}

	@Inject(
			method = "renderLevel",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V",
				ordinal = 0
			)
	)
	private void beforeDebugRender(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.invoker().beforeDebugRender(context);
	}

	@Inject(
			method = "renderLevel",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"
			)
	)
	private void onRenderParticles(CallbackInfo ci) {
		// set a flag so we know the next pushMatrix call is after particles
		didRenderParticles = true;
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"))
	private void beforeClouds(CallbackInfo ci) {
		if (didRenderParticles) {
			didRenderParticles = false;
			WorldRenderEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(context);
		}
	}

	@Inject(
			method = "renderLevel",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/renderer/LevelRenderer;renderDebug(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/Camera;)V"
			)
	)
	private void onChunkDebugRender(CallbackInfo ci) {
		WorldRenderEvents.LAST.invoker().onLast(context);
	}

	@Inject(method = "renderLevel", at = @At("RETURN"))
	private void afterRender(CallbackInfo ci) {
		WorldRenderEvents.END.invoker().onEnd(context);
	}

	@Inject(method = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}

	@Inject(at = @At("HEAD"), method = "renderSnowAndRain", cancellable = true)
	private void renderWeather(LightTexture manager, float tickDelta, double x, double y, double z, CallbackInfo info) {
		if (this.minecraft.level != null) {
			DimensionRenderingRegistry.WeatherRenderer renderer = DimensionRenderingRegistry.getWeatherRenderer(level.dimension());

			if (renderer != null) {
				renderer.render(context);
				info.cancel();
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V", cancellable = true)
	private void renderCloud(PoseStack matrices, Matrix4f matrix4f, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo info) {
		if (this.minecraft.level != null) {
			DimensionRenderingRegistry.CloudRenderer renderer = DimensionRenderingRegistry.getCloudRenderer(level.dimension());

			if (renderer != null) {
				renderer.render(context);
				info.cancel();
			}
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", shift = At.Shift.AFTER, ordinal = 0), method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", cancellable = true)
	private void renderSky(PoseStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo info) {
		if (this.minecraft.level != null) {
			DimensionRenderingRegistry.SkyRenderer renderer = DimensionRenderingRegistry.getSkyRenderer(level.dimension());

			if (renderer != null) {
				renderer.render(context);
				info.cancel();
			}
		}
	}
}
