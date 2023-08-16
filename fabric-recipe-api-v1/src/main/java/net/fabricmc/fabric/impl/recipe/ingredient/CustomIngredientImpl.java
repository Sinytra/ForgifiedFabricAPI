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

package net.fabricmc.fabric.impl.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.mixin.recipe.ingredient.CraftingHelperAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * To test this API beyond the unit tests, please refer to the recipe provider in the datagen API testmod.
 * It contains various interesting recipes to test, and explains how to package them in a datapack.
 */
public class CustomIngredientImpl extends Ingredient {
	// Static helpers used by the API

	public static final String TYPE_KEY = "fabric:type";

	@Nullable
	public static CustomIngredientSerializer<?> getSerializer(Identifier identifier) {
		IIngredientSerializer<?> serializer = getWrappedSerializer(identifier);
		return serializer instanceof ForgeCustomIngredientSerializer customSerializer ? customSerializer.unwrap() : null;
	}

	@Nullable
	public static IIngredientSerializer<?> getWrappedSerializer(Identifier identifier) {
		Objects.requireNonNull(identifier, "Identifier may not be null.");

		return CraftingHelperAccessor.getIngredients().get(identifier);
	}

	// Actual custom ingredient logic

	private final CustomIngredient customIngredient;

	public CustomIngredientImpl(CustomIngredient customIngredient) {
		super(Stream.empty());

		this.customIngredient = customIngredient;
	}

	@Override
	public CustomIngredient getCustomIngredient() {
		return customIngredient;
	}

	@Override
	public boolean requiresTesting() {
		return customIngredient.requiresTesting();
	}

	@Override
	public ItemStack[] getMatchingStacks() {
		if (this.matchingStacks == null) {
			this.matchingStacks = customIngredient.getMatchingStacks().toArray(ItemStack[]::new);
		}

		return this.matchingStacks;
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		return stack != null && customIngredient.test(stack);
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty(TYPE_KEY, customIngredient.getSerializer().getIdentifier().toString());
		customIngredient.getSerializer().write(json, coerceIngredient());
		return json;
	}

	@Override
	public boolean isEmpty() {
		// We don't want to resolve the matching stacks,
		// as this might cause the ingredient to use outdated tags when it's done too early.
		// So we just return false when the matching stacks haven't been resolved yet (i.e. when the field is null).
		return matchingStacks != null && matchingStacks.length == 0;
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return getWrappedSerializer(this.customIngredient.getSerializer().getIdentifier());
	}

	private <T> T coerceIngredient() {
		return (T) customIngredient;
	}
}
