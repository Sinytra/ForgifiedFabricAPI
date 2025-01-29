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

package net.fabricmc.fabric.mixin.object.builder;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTrades.EmeraldsForVillagerTypeItem.class)
public abstract class TradeOffersTypeAwareBuyForOneEmeraldFactoryMixin {
	/**
	 * To prevent "item" -> "air" trades, if the result of a type aware trade is air, make sure no offer is created.
	 */
	@Inject(method = "getOffer", at = @At(value = "NEW", target = "net/minecraft/world/item/trading/MerchantOffer"), cancellable = true)
	private void failOnNullItem(Entity entity, RandomSource random, CallbackInfoReturnable<MerchantOffer> cir, @Local() ItemCost tradedItem) {
		if (tradedItem.itemStack().isEmpty()) { // Will return true for an "empty" item stack that had null passed in the ctor
			cir.setReturnValue(null); // Return null to prevent creation of empty trades
		}
	}
}
