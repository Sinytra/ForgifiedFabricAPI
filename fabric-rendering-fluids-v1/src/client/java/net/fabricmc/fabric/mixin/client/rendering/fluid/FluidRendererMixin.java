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

package net.fabricmc.fabric.mixin.client.rendering.fluid;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRendererHookContainer;

@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
	@Final
	@Shadow
	private Sprite[] lavaSprites;
	@Final
	@Shadow
	private Sprite[] waterSprites;
	@Shadow
	private Sprite waterOverlaySprite;

	private final ThreadLocal<FluidRendererHookContainer> fabric_renderHandler = ThreadLocal.withInitial(FluidRendererHookContainer::new);
	private final ThreadLocal<Boolean> fabric_customRendering = ThreadLocal.withInitial(() -> false);

	@Inject(at = @At("RETURN"), method = "onResourceReload")
	public void onResourceReloadReturn(CallbackInfo info) {
		FluidRenderer self = (FluidRenderer) (Object) this;
		((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).onFluidRendererReload(self, waterSprites, lavaSprites, waterOverlaySprite);
	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void tesselate(BlockRenderView view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
		if (!fabric_customRendering.get()) {
			// Prevent recursively looking up custom fluid renderers when default behavior is being invoked
			try {
				fabric_customRendering.set(true);
				tessellateViaHandler(view, pos, vertexConsumer, blockState, fluidState, info);
			} finally {
				fabric_customRendering.set(false);
			}
		}

		if (info.isCancelled()) {
			return;
		}

		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		ctr.getSprites(view, pos, fluidState);
	}

	@Unique
	private void tessellateViaHandler(BlockRenderView view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		FluidRenderHandler handler = ((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).getOverride(fluidState.getFluid());

		ctr.view = view;
		ctr.pos = pos;
		ctr.blockState = blockState;
		ctr.fluidState = fluidState;
		ctr.handler = handler;

		if (handler != null) {
			handler.renderFluid(pos, view, vertexConsumer, blockState, fluidState);
			info.cancel();
		}
	}

	@Inject(at = @At("RETURN"), method = "render")
	public void tesselateReturn(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
		fabric_renderHandler.get().clear();
	}

	// Redirect redirects all 'waterOverlaySprite' gets in 'render' to this method, this is correct
	@Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/render/block/FluidRenderer;waterOverlaySprite:Lnet/minecraft/client/texture/Sprite;"), method = "render")
	public Sprite modWaterOverlaySprite(FluidRenderer self) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		return ctr.handler != null && ctr.hasOverlay ? ctr.overlay : waterOverlaySprite;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/extensions/common/IClientFluidTypeExtensions;getTintColor(Lnet/minecraft/fluid/FluidState;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;)I"))
	public int modTintColor(IClientFluidTypeExtensions extensions, FluidState state, BlockRenderView getter, BlockPos pos) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		if (ctr.handler != null) {
			// Include alpha in tint color
			int color = ctr.handler.getFluidColor(ctr.view, ctr.pos, ctr.fluidState);
			return 0xFF000000 | color;
		}
		return extensions.getTintColor(state, getter, pos);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;getFluidSprites(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/FluidState;)[Lnet/minecraft/client/texture/Sprite;"))
	private Sprite[] redirectSprites(BlockRenderView level, BlockPos pos, FluidState fluidStateIn) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		if (ctr.handler != null) {
			return new Sprite[] { 
					ctr.sprites[0],
					ctr.sprites[1],
					ctr.hasOverlay ? ctr.overlay : null
			};
		}
		return ForgeHooksClient.getFluidSprites(level, pos, fluidStateIn);
	}
}
