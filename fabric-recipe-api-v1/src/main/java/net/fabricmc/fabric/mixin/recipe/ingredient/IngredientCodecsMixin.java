package net.fabricmc.fabric.mixin.recipe.ingredient;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.common.crafting.IngredientCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.impl.recipe.ingredient.FabricRecipeApiV1;

@Mixin(IngredientCodecs.class)
public class IngredientCodecsMixin {

    @ModifyReturnValue(method = "codec", at = @At("RETURN"))
    private static Codec<Ingredient> modifyIngredientCodec(Codec<Ingredient> original) {
        return FabricRecipeApiV1.makeIngredientMapCodec(original);
    }
}
