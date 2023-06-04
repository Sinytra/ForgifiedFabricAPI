package net.fabricmc.fabric.impl.content.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

public final class BrewingRegistryImpl {
    public static void addContainerRecipe(Item from, Ingredient ingredient, Item to) {
        if (!(from instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + ForgeRegistries.ITEMS.getKey(from));
        } else if (!(to instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + ForgeRegistries.ITEMS.getKey(to));
        } else {
            PotionBrewing.CONTAINER_MIXES.add(new PotionBrewing.Mix<>(ForgeRegistries.ITEMS, from, ingredient, to));
        }
    }

    public static void addMix(Potion potionEntry, Ingredient potionIngredient, Potion potionResult) {
        PotionBrewing.POTION_MIXES.add(new PotionBrewing.Mix<>(ForgeRegistries.POTIONS, potionEntry, potionIngredient, potionResult));
    }
}
