package net.fabricmc.fabric.impl.recipe.ingredient.compat;

import java.util.stream.Stream;

import net.neoforged.neoforge.common.crafting.ICustomIngredient;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class FabricICustomIngredientWrapper implements CustomIngredient {
    private final ICustomIngredient ingredient;

    public FabricICustomIngredientWrapper(ICustomIngredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.ingredient.test(stack);
    }

	@Override
	public Stream<Holder<Item>> items() {
		return this.ingredient.items();
	}

    @Override
    public boolean requiresTesting() {
        return !this.ingredient.isSimple();
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        throw new UnsupportedOperationException();
    }

	@Override
	public SlotDisplay display() {
		return this.ingredient.display();
	}

	@Override
	public Ingredient toVanilla() {
		return this.ingredient.toVanilla();
	}
}
