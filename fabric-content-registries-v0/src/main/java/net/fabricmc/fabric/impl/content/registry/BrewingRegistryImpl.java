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
