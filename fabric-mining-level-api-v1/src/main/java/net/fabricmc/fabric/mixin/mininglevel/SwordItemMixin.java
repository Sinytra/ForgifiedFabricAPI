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

package net.fabricmc.fabric.mixin.mininglevel;

import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds support for {@link FabricMineableTags#SWORD_MINEABLE}.
 */
@Mixin(SwordItem.class)
abstract class SwordItemMixin extends TieredItem {
	private SwordItemMixin(Tier material, Item.Properties settings) {
		super(material, settings);
	}

	@Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
	private void fabric$onIsSuitableFor(BlockState state, CallbackInfoReturnable<Boolean> info) {
		if (state.is(FabricMineableTags.SWORD_MINEABLE) && getTier().getLevel() >= MiningLevelManager.getRequiredMiningLevel(state)) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void fabric$onGetMiningSpeedMultiplier(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> info) {
		if (info.getReturnValueF() == 1.0f) { // if not caught by vanilla checks
			if (state.is(FabricMineableTags.SWORD_MINEABLE)) { // mimics MiningToolItem.getMiningSpeedMultiplier
				info.setReturnValue(getTier().getSpeed());
			}
		}
	}
}
