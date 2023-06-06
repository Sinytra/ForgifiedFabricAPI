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

package net.fabricmc.fabric.api.item.v1;

import net.fabricmc.fabric.impl.item.FabricItemInternals;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * Fabric's version of Item.Settings. Adds additional methods and hooks
 * not found in the original class.
 *
 * <p>To use it, simply replace {@code new Item.Settings()} with
 * {@code new FabricItemSettings()}.
 */
public class FabricItemSettings extends Item.Properties {
	/**
	 * Sets the equipment slot provider of the item.
	 *
	 * @param equipmentSlotProvider the equipment slot provider
	 * @return this builder
	 */
	public FabricItemSettings equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
		FabricItemInternals.computeExtraData(this).equipmentSlot(equipmentSlotProvider);
		return this;
	}

	/**
	 * Sets the custom damage handler of the item.
	 * Note that this is only called on an ItemStack if {@link ItemStack#isDamageableItem()} returns true.
	 *
	 * @see CustomDamageHandler
	 */
	public FabricItemSettings customDamage(CustomDamageHandler handler) {
		FabricItemInternals.computeExtraData(this).customDamage(handler);
		return this;
	}

	// Overrides of vanilla methods

	@Override
	public FabricItemSettings food(FoodProperties foodComponent) {
		super.food(foodComponent);
		return this;
	}

	public FabricItemSettings maxCount(int maxCount) {
		super.stacksTo(maxCount);
		return this;
	}

	public FabricItemSettings maxDamageIfAbsent(int maxDamage) {
		super.defaultDurability(maxDamage);
		return this;
	}

	public FabricItemSettings maxDamage(int maxDamage) {
		super.durability(maxDamage);
		return this;
	}

	public FabricItemSettings recipeRemainder(Item recipeRemainder) {
		super.craftRemainder(recipeRemainder);
		return this;
	}

	@Override
	public FabricItemSettings rarity(Rarity rarity) {
		super.rarity(rarity);
		return this;
	}

	public FabricItemSettings fireproof() {
		super.fireResistant();
		return this;
	}

	public FabricItemSettings requires(FeatureFlag... features) {
		super.requiredFeatures(features);
		return this;
	}
}
