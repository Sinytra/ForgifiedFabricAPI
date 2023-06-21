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

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRendererHookContainer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlockRenderer.class)
public class FluidRendererMixin {
	@Shadow
	private TextureAtlasSprite waterOverlay;

	private final ThreadLocal<FluidRendererHookContainer> fabric_renderHandler = ThreadLocal.withInitial(FluidRendererHookContainer::new);
	private final ThreadLocal<Boolean> fabric_customRendering = ThreadLocal.withInitial(() -> false);

	@Inject(at = @At("RETURN"), method = "setupSprites")
	public void onResourceReloadReturn(CallbackInfo info) {
		LiquidBlockRenderer self = (LiquidBlockRenderer) (Object) this;
		((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).onFluidRendererReload(self);
	}

	@Inject(at = @At("HEAD"), method = "tesselate", cancellable = true)
	public void tesselate(BlockAndTintGetter view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
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
	private void tessellateViaHandler(BlockAndTintGetter view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		FluidRenderHandler handler = ((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).getOverride(fluidState.getType());

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

	@Inject(at = @At("RETURN"), method = "tesselate")
	public void tesselateReturn(BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
		fabric_renderHandler.get().clear();
	}

	// Redirect redirects all 'waterOverlaySprite' gets in 'render' to this method, this is correct
	@Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;waterOverlay:Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"), method = "tesselate")
	public TextureAtlasSprite modWaterOverlaySprite(LiquidBlockRenderer self) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		return ctr.handler != null && ctr.hasOverlay ? ctr.overlay : waterOverlay;
	}

	@Redirect(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/extensions/common/IClientFluidTypeExtensions;getTintColor(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
	public int modTintColor(IClientFluidTypeExtensions extensions, FluidState state, BlockAndTintGetter getter, BlockPos pos) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		return ctr.handler != null ? ctr.handler.getFluidColor(ctr.view, ctr.pos, ctr.fluidState) : extensions.getTintColor(state, getter, pos);
	}

	@Redirect(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;getFluidSprites(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;)[Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
	private TextureAtlasSprite[] redirectSprites(BlockAndTintGetter level, BlockPos pos, FluidState fluidStateIn) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		if (ctr.handler != null) {
			return new TextureAtlasSprite[] {
					ctr.sprites[0],
					ctr.sprites[1],
					ctr.hasOverlay ? ctr.overlay : null
			};
		}
		return ForgeHooksClient.getFluidSprites(level, pos, fluidStateIn);
	}
}
