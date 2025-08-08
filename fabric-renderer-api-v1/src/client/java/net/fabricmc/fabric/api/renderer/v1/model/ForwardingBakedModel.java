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

package net.fabricmc.fabric.api.renderer.v1.model;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for specialized model implementations that need to wrap other baked models.
 * Avoids boilerplate code for pass-through methods.
 */
public abstract class ForwardingBakedModel implements BakedModel, WrapperBakedModel {
	/** implementations must set this somehow. */
	protected BakedModel wrapped;

	protected ForwardingBakedModel(BakedModel wrapped) {
		this.wrapped = wrapped;
	}

	public ForwardingBakedModel() {
	}

	@Override
	public boolean isVanillaAdapter() {
		return wrapped.isVanillaAdapter();
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		wrapped.emitBlockQuads(blockView, state, pos, randomSupplier, context);
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		wrapped.emitItemQuads(stack, randomSupplier, context);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState blockState, Direction face, RandomSource rand) {
		return wrapped.getQuads(blockState, face, rand);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return wrapped.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return wrapped.isGui3d();
	}

	@Override
	public boolean isCustomRenderer() {
		return wrapped.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return wrapped.getParticleIcon();
	}

	@Override
	public boolean usesBlockLight() {
		return wrapped.usesBlockLight();
	}

	@Override
	public ItemTransforms getTransforms() {
		return wrapped.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		return wrapped.getOverrides();
	}

	@Override
	public BakedModel getWrappedModel() {
		return wrapped;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
		return wrapped.getQuads(state, side, rand, data, renderType);
	}

	@Override
	public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
		return wrapped.useAmbientOcclusion(state, data, renderType);
	}

	@Override
	public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
		BakedModel result = wrapped.applyTransform(transformType, poseStack, applyLeftHandTransform);

		if (result == wrapped) {
		    return this;
		}

		return result;
	}

	@Override
	public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
		return wrapped.getModelData(level, pos, state, modelData);
	}

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		return wrapped.getParticleIcon(data);
	}

	@Override
	public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
		return wrapped.getRenderTypes(state, rand, data);
	}

	@Override
	public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
		return wrapped.getRenderTypes(itemStack, fabulous);
	}

	@Override
	public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
		return wrapped.getRenderPasses(itemStack, fabulous);
	}
}
