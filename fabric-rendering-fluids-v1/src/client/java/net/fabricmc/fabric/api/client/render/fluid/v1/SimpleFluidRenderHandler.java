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

package net.fabricmc.fabric.api.client.render.fluid.v1;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A simple fluid render handler that uses and loads sprites given by their
 * identifiers. Most fluids don't need more than this. In fact, if a fluid just
 * needs the vanilla water texture with a custom color, {@link #coloredWater}
 * can be used to easily create a fluid render handler for that.
 *
 * <p>Note that it's assumed that the fluid textures are assumed to be
 * registered to the blocks sprite atlas. If they are not, you have to manually
 * register the fluid textures. The "fabric-textures" API may come in handy for
 * that.
 */
public class SimpleFluidRenderHandler implements FluidRenderHandler {
	/**
	 * The vanilla still water texture identifier.
	 */
	public static final ResourceLocation WATER_STILL = new ResourceLocation("block/water_still");

	/**
	 * The vanilla flowing water texture identifier.
	 */
	public static final ResourceLocation WATER_FLOWING = new ResourceLocation("block/water_flow");

	/**
	 * The vanilla water overlay texture identifier.
	 */
	public static final ResourceLocation WATER_OVERLAY = new ResourceLocation("block/water_overlay");

	/**
	 * The vanilla still lava texture identifier.
	 */
	public static final ResourceLocation LAVA_STILL = new ResourceLocation("block/lava_still");

	/**
	 * The vanilla flowing lava texture identifier.
	 */
	public static final ResourceLocation LAVA_FLOWING = new ResourceLocation("block/lava_flow");

	protected final ResourceLocation stillTexture;
	protected final ResourceLocation flowingTexture;
	protected final ResourceLocation overlayTexture;

	protected final TextureAtlasSprite[] sprites;

	protected final int tint;

	/**
	 * Creates a fluid render handler with an overlay texture and a custom,
	 * fixed tint.
	 *
	 * @param stillTexture The texture for still fluid.
	 * @param flowingTexture The texture for flowing/falling fluid.
	 * @param overlayTexture The texture behind glass, leaves and other
	 * {@linkplain FluidRenderHandlerRegistry#setBlockTransparency registered
	 * transparent blocks}.
	 * @param tint The fluid color RGB. Alpha is ignored.
	 */
	public SimpleFluidRenderHandler(ResourceLocation stillTexture, ResourceLocation flowingTexture, @Nullable ResourceLocation overlayTexture, int tint) {
		this.stillTexture = Objects.requireNonNull(stillTexture, "stillTexture");
		this.flowingTexture = Objects.requireNonNull(flowingTexture, "flowingTexture");;
		this.overlayTexture = overlayTexture;
		this.sprites = new TextureAtlasSprite[overlayTexture == null ? 2 : 3];
		this.tint = tint;
	}

	/**
	 * Creates a fluid render handler with an overlay texture and no tint.
	 *
	 * @param stillTexture The texture for still fluid.
	 * @param flowingTexture The texture for flowing/falling fluid.
	 * @param overlayTexture The texture behind glass, leaves and other
	 * {@linkplain FluidRenderHandlerRegistry#setBlockTransparency registered
	 * transparent blocks}.
	 */
	public SimpleFluidRenderHandler(ResourceLocation stillTexture, ResourceLocation flowingTexture, ResourceLocation overlayTexture) {
		this(stillTexture, flowingTexture, overlayTexture, -1);
	}

	/**
	 * Creates a fluid render handler without an overlay texture and a custom,
	 * fixed tint.
	 *
	 * @param stillTexture The texture for still fluid.
	 * @param flowingTexture The texture for flowing/falling fluid.
	 * @param tint The fluid color RGB. Alpha is ignored.
	 */
	public SimpleFluidRenderHandler(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tint) {
		this(stillTexture, flowingTexture, null, tint);
	}

	/**
	 * Creates a fluid render handler without an overlay texture and no tint.
	 *
	 * @param stillTexture The texture for still fluid.
	 * @param flowingTexture The texture for flowing/falling fluid.
	 */
	public SimpleFluidRenderHandler(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
		this(stillTexture, flowingTexture, null, -1);
	}

	/**
	 * Creates a fluid render handler that uses the vanilla water texture with a
	 * fixed, custom color.
	 *
	 * @param tint The fluid color RGB. Alpha is ignored.
	 * @see	#WATER_STILL
	 * @see	#WATER_FLOWING
	 * @see #WATER_OVERLAY
	 */
	public static SimpleFluidRenderHandler coloredWater(int tint) {
		return new SimpleFluidRenderHandler(WATER_STILL, WATER_FLOWING, WATER_OVERLAY, tint);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
		return sprites;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reloadTextures(TextureAtlas textureAtlas) {
		sprites[0] = textureAtlas.getSprite(stillTexture);
		sprites[1] = textureAtlas.getSprite(flowingTexture);

		if (overlayTexture != null) {
			sprites[2] = textureAtlas.getSprite(overlayTexture);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
		return tint;
	}
}
