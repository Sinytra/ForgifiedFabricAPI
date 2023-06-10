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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NbtIngredient implements CustomIngredient {
	public static final CustomIngredientSerializer<NbtIngredient> SERIALIZER = new Serializer();

	private final Ingredient base;
	@Nullable
	private final CompoundTag nbt;
	private final boolean strict;

	public NbtIngredient(Ingredient base, @Nullable CompoundTag nbt, boolean strict) {
		if (nbt == null && !strict) {
			throw new IllegalArgumentException("NbtIngredient can only have null NBT in strict mode");
		}

		this.base = base;
		this.nbt = nbt;
		this.strict = strict;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (!base.test(stack)) return false;

		if (strict) {
			return Objects.equals(nbt, stack.getTag());
		} else {
			return NbtUtils.compareNbt(nbt, stack.getTag(), true);
		}
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		List<ItemStack> stacks = new ArrayList<>(List.of(base.getItems()));
		stacks.replaceAll(stack -> {
			ItemStack copy = stack.copy();

			if (nbt != null) {
				copy.setTag(nbt.copy());
			}

			return copy;
		});
		stacks.removeIf(stack -> !base.test(stack));
		return stacks;
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private static class Serializer implements CustomIngredientSerializer<NbtIngredient> {
		private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		private final ResourceLocation id = new ResourceLocation("fabric", "nbt");

		@Override
		public ResourceLocation getIdentifier() {
			return id;
		}

		@Override
		public NbtIngredient read(JsonObject json) {
			Ingredient base = Ingredient.fromJson(json.get("base"));
			CompoundTag nbt = readNbt(json.get("nbt"));
			boolean strict = GsonHelper.getAsBoolean(json, "strict", false);
			return new NbtIngredient(base, nbt, strict);
		}

		/**
		 * Inspiration taken from {@link net.minecraft.advancements.critereon.NbtPredicate#fromJson}.
		 */
		@Nullable
		private static CompoundTag readNbt(@Nullable JsonElement json) {
			// Process null
			if (json == null || json.isJsonNull()) {
				return null;
			}

			try {
				if (json.isJsonObject()) {
					// We use a normal .toString() to convert the json to string, and read it as SNBT.
					// Using DynamicOps would mess with the type of integers and cause things like damage comparisons to fail...
					return TagParser.parseTag(json.toString());
				} else {
					// Assume it's a string representation of the NBT
					return TagParser.parseTag(GsonHelper.convertToString(json, "nbt"));
				}
			} catch (CommandSyntaxException commandSyntaxException) {
				throw new JsonSyntaxException("Invalid nbt tag: " + commandSyntaxException.getMessage());
			}
		}

		@Override
		public void write(JsonObject json, NbtIngredient ingredient) {
			json.add("base", ingredient.base.toJson());
			json.addProperty("strict", ingredient.strict);

			if (ingredient.nbt != null) {
				json.add("nbt", NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, ingredient.nbt));
			}
		}

		@Override
		public NbtIngredient read(FriendlyByteBuf buf) {
			Ingredient base = Ingredient.fromNetwork(buf);
			CompoundTag nbt = buf.readNbt();
			boolean strict = buf.readBoolean();
			return new NbtIngredient(base, nbt, strict);
		}

		@Override
		public void write(FriendlyByteBuf buf, NbtIngredient ingredient) {
			ingredient.base.toNetwork(buf);
			buf.writeNbt(ingredient.nbt);
			buf.writeBoolean(ingredient.strict);
		}
	}
}
