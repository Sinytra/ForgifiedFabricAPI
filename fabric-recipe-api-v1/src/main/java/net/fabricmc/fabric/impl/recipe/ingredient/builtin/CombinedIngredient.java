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

package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Function;

/**
 * Base class for ALL and ANY ingredients.
 */
abstract class CombinedIngredient implements CustomIngredient {
	protected final Ingredient[] ingredients;

	protected CombinedIngredient(Ingredient[] ingredients) {
		if (ingredients.length == 0) {
			throw new IllegalArgumentException("ALL or ANY ingredient must have at least one sub-ingredient");
		}

		this.ingredients = ingredients;
	}

	@Override
	public boolean requiresTesting() {
		for (Ingredient ingredient : ingredients) {
			if (((FabricIngredient) ingredient).requiresTesting()) {
				return true;
			}
		}

		return false;
	}

	static class Serializer<I extends CombinedIngredient> implements CustomIngredientSerializer<I> {
		private final ResourceLocation identifier;
		private final Function<Ingredient[], I> factory;

		Serializer(ResourceLocation identifier, Function<Ingredient[], I> factory) {
			this.identifier = identifier;
			this.factory = factory;
		}

		@Override
		public ResourceLocation getIdentifier() {
			return identifier;
		}

		@Override
		public I read(JsonObject json) {
			JsonArray values = GsonHelper.getAsJsonArray(json, "ingredients");
			Ingredient[] ingredients = new Ingredient[values.size()];

			for (int i = 0; i < values.size(); i++) {
				ingredients[i] = Ingredient.fromJson(values.get(i));
			}

			return factory.apply(ingredients);
		}

		@Override
		public void write(JsonObject json, I ingredient) {
			JsonArray values = new JsonArray();

			for (Ingredient value : ingredient.ingredients) {
				values.add(value.toJson());
			}

			json.add("ingredients", values);
		}

		@Override
		public I read(FriendlyByteBuf buf) {
			int size = buf.readVarInt();
			Ingredient[] ingredients = new Ingredient[size];

			for (int i = 0; i < size; i++) {
				ingredients[i] = Ingredient.fromNetwork(buf);
			}

			return factory.apply(ingredients);
		}

		@Override
		public void write(FriendlyByteBuf buf, I ingredient) {
			buf.writeVarInt(ingredient.ingredients.length);

			for (Ingredient value : ingredient.ingredients) {
				value.toNetwork(buf);
			}
		}
	}
}
