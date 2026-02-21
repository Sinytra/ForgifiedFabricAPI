package net.fabricmc.fabric.mixin.datagen.recipe;

import net.fabricmc.fabric.api.datagen.v1.recipe.FabricRecipeExporter;
import net.minecraft.data.recipes.RecipeOutput;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RecipeOutput.class)
public interface RecipeOutputMixin extends FabricRecipeExporter {
}
