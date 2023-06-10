package net.fabricmc.fabric.impl.recipe.ingredient;

import net.fabricmc.fabric.impl.recipe.ingredient.client.CustomIngredientSyncClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_recipe_api_v1")
public class RecipesImpl {

    public RecipesImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            CustomIngredientSyncClient.onInitializeClient();
        }
        CustomIngredientInit.onInitialize();
        CustomIngredientSync.onInitialize();
    }
}
