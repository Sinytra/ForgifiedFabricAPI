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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

@Mixin(TradeOffers.TypeAwareBuyForOneEmeraldFactory.class)
public abstract class TradeOffersTypeAwareBuyForOneEmeraldFactoryMixin {
	/**
	 * To prevent crashes due to passing a {@code null} item to a {@link TradeOffer}, return a {@code null} trade offer
	 * early before {@code null} is passed to the constructor.
	 */
	@ModifyExpressionValue(
			method = "create",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
	)
	private Object failOnNullItem(Object item, @Cancellable CallbackInfoReturnable<TradeOffer> cir) {
		if (item == null) {
			cir.setReturnValue(null);
		}

		return item;
	}
}
