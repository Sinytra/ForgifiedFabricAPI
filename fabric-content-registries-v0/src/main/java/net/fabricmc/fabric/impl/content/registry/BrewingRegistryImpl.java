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

package net.fabricmc.fabric.impl.content.registry;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.item.Item;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;

public final class BrewingRegistryImpl {
    public static void addContainerRecipe(Item from, Ingredient ingredient, Item to) {
        if (!(from instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + ForgeRegistries.ITEMS.getKey(from));
        } else if (!(to instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + ForgeRegistries.ITEMS.getKey(to));
        } else {
			BrewingRecipeRegistry.ITEM_RECIPES.add(new BrewingRecipeRegistry.Recipe<>(ForgeRegistries.ITEMS, from, ingredient, to));
        }
    }

    public static void addMix(Potion potionEntry, Ingredient potionIngredient, Potion potionResult) {
		BrewingRecipeRegistry.POTION_RECIPES.add(new BrewingRecipeRegistry.Recipe<>(ForgeRegistries.POTIONS, potionEntry, potionIngredient, potionResult));
    }
}
