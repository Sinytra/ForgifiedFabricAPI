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

package net.fabricmc.fabric.test.renderer.simple.client;

import net.fabricmc.fabric.api.block.v1.FabricBlockState;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.test.renderer.simple.RendererTest;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Very crude implementation of a pillar block model that connects with pillars above and below.
 */
public class PillarBakedModel implements BakedModel, FabricBakedModel {
	private enum ConnectedTexture {
		ALONE, BOTTOM, MIDDLE, TOP
	}

	// alone, bottom, middle, top
	private final TextureAtlasSprite[] sprites;
	private final RenderMaterial defaultMaterial;
	private final RenderMaterial glintMaterial;

	public PillarBakedModel(TextureAtlasSprite[] sprites) {
		this.sprites = sprites;

		MaterialFinder finder = RendererAccess.INSTANCE.getRenderer().materialFinder();
		defaultMaterial = finder.find();
		finder.clear();
		glintMaterial = finder.glint(TriState.TRUE).find();
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		emitQuads(context.getEmitter(), blockView, state, pos);
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		emitQuads(context.getEmitter(), null, null, null);
	}

	private void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter blockView, @Nullable BlockState state, @Nullable BlockPos pos) {
		for (Direction side : Direction.values()) {
			ConnectedTexture texture = ConnectedTexture.ALONE;
			RenderMaterial material = defaultMaterial;

			if (side.getAxis().isHorizontal()) {
				if (blockView != null && state != null && pos != null) {
					boolean connectAbove = canConnect(blockView, pos.relative(Direction.UP), side, state, pos);
					boolean connectBelow = canConnect(blockView, pos.relative(Direction.DOWN), side, state, pos);

					if (connectAbove && connectBelow) {
						texture = ConnectedTexture.MIDDLE;
					} else if (connectAbove) {
						texture = ConnectedTexture.BOTTOM;
					} else if (connectBelow) {
						texture = ConnectedTexture.TOP;
					}
				}

				material = glintMaterial;
			}

			emitter.square(side, 0, 0, 1, 1, 0);
			emitter.spriteBake(sprites[texture.ordinal()], MutableQuadView.BAKE_LOCK_UV);
			emitter.color(-1, -1, -1, -1);
			emitter.material(material);
			emitter.emit();
		}
	}

	private static boolean canConnect(BlockAndTintGetter blockView, BlockPos pos, Direction side, BlockState sourceState, BlockPos sourcePos) {
		// In this testmod we can't rely on injected interfaces - in normal mods the (FabricBlockState) cast will be unnecessary
		return blockView.getBlockState(pos).getAppearance(blockView, pos, side, sourceState, sourcePos).is(RendererTest.PILLAR.get());
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
		return List.of();
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return true;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return sprites[0];
	}

	@Override
	public ItemTransforms getTransforms() {
		return ModelHelper.MODEL_TRANSFORM_BLOCK;
	}

	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}
}
