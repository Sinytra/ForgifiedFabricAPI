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

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class DifferenceIngredient implements CustomIngredient {
	public static final CustomIngredientSerializer<DifferenceIngredient> SERIALIZER = new Serializer();

	private final Ingredient base;
	private final Ingredient subtracted;

	public DifferenceIngredient(Ingredient base, Ingredient subtracted) {
		this.base = base;
		this.subtracted = subtracted;
	}

	@Override
	public boolean test(ItemStack stack) {
		return base.test(stack) && !subtracted.test(stack);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		List<ItemStack> stacks = new ArrayList<>(List.of(base.getItems()));
		stacks.removeIf(subtracted);
		return stacks;
	}

	@Override
	public boolean requiresTesting() {
		return ((FabricIngredient) base).requiresTesting() || ((FabricIngredient) subtracted).requiresTesting();
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private static class Serializer implements CustomIngredientSerializer<DifferenceIngredient> {
		private final ResourceLocation id = new ResourceLocation("fabric", "difference");

		@Override
		public ResourceLocation getIdentifier() {
			return id;
		}

		@Override
		public DifferenceIngredient read(JsonObject json) {
			Ingredient base = Ingredient.fromJson(json.get("base"));
			Ingredient subtracted = Ingredient.fromJson(json.get("subtracted"));
			return new DifferenceIngredient(base, subtracted);
		}

		@Override
		public void write(JsonObject json, DifferenceIngredient ingredient) {
			json.add("base", ingredient.base.toJson());
			json.add("subtracted", ingredient.subtracted.toJson());
		}

		@Override
		public DifferenceIngredient read(FriendlyByteBuf buf) {
			Ingredient base = Ingredient.fromNetwork(buf);
			Ingredient subtracted = Ingredient.fromNetwork(buf);
			return new DifferenceIngredient(base, subtracted);
		}

		@Override
		public void write(FriendlyByteBuf buf, DifferenceIngredient ingredient) {
			ingredient.base.toNetwork(buf);
			ingredient.subtracted.toNetwork(buf);
		}
	}
}
