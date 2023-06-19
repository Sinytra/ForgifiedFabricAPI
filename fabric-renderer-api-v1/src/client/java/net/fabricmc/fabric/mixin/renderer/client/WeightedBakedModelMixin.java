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
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(WeightedBakedModel.class)
public class WeightedBakedModelMixin implements FabricBakedModel {
	@Shadow
	@Final
	private int totalWeight;
	@Shadow
	@Final
	private List<WeightedEntry.Wrapper<BakedModel>> list;
	@Unique
	boolean isVanilla = true;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void onInit(List<WeightedEntry.Wrapper<BakedModel>> models, CallbackInfo cb) {
		for (int i = 0; i < models.size(); i++) {
			if (!((FabricBakedModel) models.get(i).getData()).isVanillaAdapter()) {
				isVanilla = false;
				break;
			}
		}
	}

	@Override
	public boolean isVanillaAdapter() {
		return isVanilla;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		WeightedEntry.Wrapper<BakedModel> selected = WeightedRandom.getWeightedItem(this.list, Math.abs((int) randomSupplier.get().nextLong()) % this.totalWeight).orElse(null);

		if (selected != null) {
			((FabricBakedModel) selected.getData()).emitBlockQuads(blockView, state, pos, randomSupplier, context);
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		WeightedEntry.Wrapper<BakedModel> selected = WeightedRandom.getWeightedItem(this.list, Math.abs((int) randomSupplier.get().nextLong()) % this.totalWeight).orElse(null);

		if (selected != null) {
			((FabricBakedModel) selected.getData()).emitItemQuads(stack, randomSupplier, context);
		}
	}
}
