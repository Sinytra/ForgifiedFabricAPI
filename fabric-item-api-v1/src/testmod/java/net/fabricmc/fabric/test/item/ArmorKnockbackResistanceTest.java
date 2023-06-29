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

package net.fabricmc.fabric.test.item;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class ArmorKnockbackResistanceTest {
	private static final ArmorMaterial WOOD_ARMOR = new ArmorMaterial() {
		@Override
		public int getDurability(ArmorItem.Type arg) {
			return 50;
		}

		@Override
		public int getProtection(ArmorItem.Type arg) {
			return 5;
		}

		@Override
		public int getEnchantability() {
			return 1;
		}

		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.fromTag(ItemTags.LOGS);
		}

		@Override
		public String getName() {
			return "wood";
		}

		@Override
		public float getToughness() {
			return 0.0F;
		}

		@Override
		public float getKnockbackResistance() {
			return 0.5F;
		}
	};

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FabricItemTestsImpl.MODID);
	private static final RegistryObject<Item> WOODEN_BOOTS = ITEMS.register("wooden_boots", () -> new ArmorItem(WOOD_ARMOR, ArmorItem.Type.BOOTS, new Item.Settings()));

	public static void onInitialize(IEventBus bus) {
		ITEMS.register(bus);
	}
}
