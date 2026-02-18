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

package net.fabricmc.fabric.mixin.renderer.client;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.MultipartModelData;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(MultiPartBakedModel.class)
public class MultipartBakedModelMixin implements FabricBakedModel {
	@Shadow
	@Final
	private List<Pair<Predicate<BlockState>, BakedModel>> selectors;

	@Shadow
	@Final
	private Map<BlockState, BitSet> selectorCache;

	@Unique
	boolean isVanilla = true;

	@Override
	public boolean isVanillaAdapter() {
		return isVanilla;
	}

	@Inject(at = @At("RETURN"), method = "<init>")
	private void onInit(List<Pair<Predicate<BlockState>, BakedModel>> components, CallbackInfo cb) {
		for (Pair<Predicate<BlockState>, BakedModel> component : components) {
			if (!component.getRight().isVanillaAdapter()) {
				isVanilla = false;
				break;
			}
		}
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		BitSet bitSet = this.selectorCache.get(state);

		if (bitSet == null) {
			bitSet = new BitSet();

			for (int i = 0; i < this.selectors.size(); i++) {
				Pair<Predicate<BlockState>, BakedModel> pair = selectors.get(i);

				if (pair.getLeft().test(state)) {
					bitSet.set(i);
				}
			}

			selectorCache.put(state, bitSet);
		}

		RandomSource random = randomSupplier.get();
		// Imitate vanilla passing a new random to the submodels
		long randomSeed = random.nextLong();
		Supplier<RandomSource> subModelRandomSupplier = () -> {
			random.setSeed(randomSeed);
			return random;
		};

		for (int i = 0; i < this.selectors.size(); i++) {
			if (bitSet.get(i)) {
				BakedModel model = selectors.get(i).getRight();
				ModelData data = MultipartModelData.resolve(context.getModelData(), model);

				RenderType renderType = context.getRenderType();
				if (renderType != null && !model.getRenderTypes(state, random, context.getModelData()).contains(renderType))
					continue;

				context.pushModelData(data);
				model.emitBlockQuads(blockView, state, pos, subModelRandomSupplier, context);
				context.popModelData();
			}
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		// Vanilla doesn't use MultipartBakedModel for items.
	}
}
