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

import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.compat.FabricICustomIngredientWrapper;
import net.fabricmc.fabric.impl.recipe.ingredient.compat.NeoCustomIngredientWrapper;

@Mixin(Ingredient.class)
public class IngredientMixin implements FabricIngredient {
	@Nullable
	@Shadow
	private ICustomIngredient customIngredient;

	@Override
	public @Nullable CustomIngredient getCustomIngredient() {
		return customIngredient != null
				? customIngredient instanceof NeoCustomIngredientWrapper(
				CustomIngredient ingredient
		) ? ingredient : new FabricICustomIngredientWrapper(customIngredient)
				: null;
	}
}
