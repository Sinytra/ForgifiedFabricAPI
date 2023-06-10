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

package net.fabricmc.fabric.mixin.recipe.ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.ShapelessMatch;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {
	@Final
	@Shadow
	NonNullList<Ingredient> ingredients;
	@Unique
	private boolean fabric_requiresTesting = false;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void cacheRequiresTesting(ResourceLocation id, String group, CraftingBookCategory category, ItemStack output, NonNullList<Ingredient> input, CallbackInfo ci) {
		for (Ingredient ingredient : input) {
			if (((FabricIngredient) ingredient).requiresTesting()) {
				fabric_requiresTesting = true;
				break;
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "matches", cancellable = true)
	public void customIngredientMatch(CraftingContainer craftingInventory, Level world, CallbackInfoReturnable<Boolean> cir) {
		if (fabric_requiresTesting) {
			List<ItemStack> nonEmptyStacks = new ArrayList<>(craftingInventory.getContainerSize());

			for (int i = 0; i < craftingInventory.getContainerSize(); ++i) {
				ItemStack stack = craftingInventory.getItem(i);

				if (!stack.isEmpty()) {
					nonEmptyStacks.add(stack);
				}
			}

			cir.setReturnValue(ShapelessMatch.isMatch(nonEmptyStacks, ingredients));
		}
	}
}
