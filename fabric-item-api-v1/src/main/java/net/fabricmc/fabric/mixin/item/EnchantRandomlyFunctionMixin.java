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

package net.fabricmc.fabric.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;

@Mixin(EnchantRandomlyFunction.class)
abstract class EnchantRandomlyFunctionMixin {
	@WrapOperation(
			method = "lambda$run$1",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;supportsEnchantment(Lnet/minecraft/core/Holder;)Z")
	)
	private static boolean callAllowEnchantingEvent(ItemStack stack, Holder<Enchantment> registryEntry, Operation<Boolean> original) {
		return stack.canBeEnchantedWith(registryEntry, EnchantingContext.ACCEPTABLE) || original.call(stack, registryEntry);
	}
}
