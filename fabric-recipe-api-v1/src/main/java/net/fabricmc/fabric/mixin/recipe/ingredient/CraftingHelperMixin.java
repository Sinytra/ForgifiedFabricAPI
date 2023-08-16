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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.JsonHelper;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CraftingHelper.class)
public class CraftingHelperMixin {
    @Unique
    private static final ThreadLocal<Boolean> FABRIC_JSON_ARRAY_DESERIALIZATION = ThreadLocal.withInitial(() -> false);

    @Inject(method = "lambda$getIngredient$0", at = @At("HEAD"))
    private static void beforeJsonArrayDeserialize(boolean allowEmpty, List<Ingredient> vanilla, List<Ingredient> ingredients, JsonElement ele, CallbackInfo ci) {
        FABRIC_JSON_ARRAY_DESERIALIZATION.set(true);
    }

    @Inject(method = "lambda$getIngredient$0", at = @At("TAIL"))
    private static void afterJsonArrayDeserialize(boolean allowEmpty, List<Ingredient> vanilla, List<Ingredient> ingredients, JsonElement ele, CallbackInfo ci) {
        FABRIC_JSON_ARRAY_DESERIALIZATION.set(false);
    }

    @Redirect(method = "getIngredient(Lcom/google/gson/JsonElement;Z)Lnet/minecraft/recipe/Ingredient;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;getString(Lcom/google/gson/JsonObject;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"), remap = false)
    private static String modifyIngredientTypeValue(JsonObject object, String element, String defaultStr) {
        if (object.has(CustomIngredientImpl.TYPE_KEY)) {
            // Throw exception when someone attempts to use our custom key inside an array ingredient.
            // The {@link AnyIngredient} should be used instead.
            if (FABRIC_JSON_ARRAY_DESERIALIZATION.get()) {
                throw new IllegalArgumentException("Custom ingredient cannot be used inside an array ingredient. You can replace the array by a fabric:any ingredient.");
            }
            element = CustomIngredientImpl.TYPE_KEY;
        }
        return JsonHelper.getString(object, element, defaultStr);
    }
}
