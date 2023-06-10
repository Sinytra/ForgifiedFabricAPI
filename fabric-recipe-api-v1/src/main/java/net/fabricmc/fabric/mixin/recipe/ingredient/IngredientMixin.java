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
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public class IngredientMixin implements FabricIngredient {
    /**
     * Inject right when vanilla detected a json object and check for our custom key.
     *
     * @implNote FFAPI: Inject before forge checks for its ingredient serializers.
     */
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/crafting/CraftingHelper;getIngredient(Lcom/google/gson/JsonElement;)Lnet/minecraft/world/item/crafting/Ingredient;",
            ordinal = 0
        ),
        method = "fromJson",
        cancellable = true
    )
    private static void injectFromJson(JsonElement json, CallbackInfoReturnable<Ingredient> cir) {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();

            if (obj.has(CustomIngredientImpl.TYPE_KEY)) {
                ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(obj, CustomIngredientImpl.TYPE_KEY));
                CustomIngredientSerializer<?> serializer = CustomIngredientSerializer.get(id);

                if (serializer != null) {
                    cir.setReturnValue(serializer.read(obj).toVanilla());
                } else {
                    throw new IllegalArgumentException("Unknown custom ingredient type: " + id);
                }
            }
        }
    }

    /**
     * Throw exception when someone attempts to use our custom key inside an array ingredient.
     * The {@link AnyIngredient} should be used instead.
     */
    @Inject(at = @At("HEAD"), method = "valueFromJson")
    private static void injectEntryFromJson(JsonObject obj, CallbackInfoReturnable<?> cir) {
        if (obj.has(CustomIngredientImpl.TYPE_KEY)) {
            throw new IllegalArgumentException("Custom ingredient cannot be used inside an array ingredient. You can replace the array by a fabric:any ingredient.");
        }
    }

    @Inject(
        at = @At("HEAD"),
        method = "fromNetwork",
        cancellable = true
    )
    private static void injectFromPacket(FriendlyByteBuf buf, CallbackInfoReturnable<Ingredient> cir) {
        int index = buf.readerIndex();

        if (buf.readVarInt() == CustomIngredientImpl.PACKET_MARKER) {
            ResourceLocation type = buf.readResourceLocation();
            CustomIngredientSerializer<?> serializer = CustomIngredientSerializer.get(type);

            if (serializer == null) {
                throw new IllegalArgumentException("Cannot deserialize custom ingredient of unknown type " + type);
            }

            cir.setReturnValue(serializer.read(buf).toVanilla());
        } else {
            // Reset index for vanilla's normal deserialization logic.
            buf.readerIndex(index);
        }
    }

    @Inject(method = "toNetwork", at = @At("HEAD"))
    private void injectToNetwork(FriendlyByteBuf buf, CallbackInfo ci) {
        if ((Ingredient) (Object) this instanceof CustomIngredientImpl customIngredient) {
            customIngredient.fabric_toNetwork(buf);
            ci.cancel();
        }
    }
}
