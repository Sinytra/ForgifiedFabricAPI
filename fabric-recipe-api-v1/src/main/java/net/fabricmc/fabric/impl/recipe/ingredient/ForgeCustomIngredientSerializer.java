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

import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import net.minecraft.network.PacketByteBuf;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ForgeCustomIngredientSerializer implements IIngredientSerializer<CustomIngredientImpl> {
	private final CustomIngredientSerializer serializer;

	public ForgeCustomIngredientSerializer(CustomIngredientSerializer serializer) {
		this.serializer = serializer;
	}

	public <T extends CustomIngredient> CustomIngredientSerializer<T> unwrap() {
		return (CustomIngredientSerializer<T>) this.serializer;
	}

	@Override
	public CustomIngredientImpl parse(PacketByteBuf buf) {
		return (CustomIngredientImpl) this.serializer.read(buf).toVanilla();
	}

	@Override
	public CustomIngredientImpl parse(JsonObject json) {
		return (CustomIngredientImpl) this.serializer.read(json).toVanilla();
	}

	@Override
	public void write(PacketByteBuf buf, CustomIngredientImpl ingredient) {
		this.serializer.write(buf, ingredient.getCustomIngredient());
	}
}
