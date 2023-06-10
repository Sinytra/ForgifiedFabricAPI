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

package net.fabricmc.fabric.test.recipe.ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Objects;

@GameTestHolder(IngredientTestsImpl.MODID)
public class IngredientMatchTests {
    @GameTest(templateNamespace = IngredientTestsImpl.MODID, template = "empty")
    @PrefixGameTestTemplate(false)
    public void testAllIngredient(GameTestHelper context) {
        Ingredient allIngredient = DefaultCustomIngredients.all(Ingredient.of(Items.APPLE, Items.CARROT), Ingredient.of(Items.STICK, Items.CARROT));

        assertEquals(1, allIngredient.getItems().length);
        assertEquals(Items.CARROT, allIngredient.getItems()[0].getItem());
        assertEquals(false, allIngredient.isEmpty());

        assertEquals(false, allIngredient.test(new ItemStack(Items.APPLE)));
        assertEquals(true, allIngredient.test(new ItemStack(Items.CARROT)));
        assertEquals(false, allIngredient.test(new ItemStack(Items.STICK)));

        Ingredient emptyAllIngredient = DefaultCustomIngredients.all(Ingredient.of(Items.APPLE), Ingredient.of(Items.STICK));

        assertEquals(0, emptyAllIngredient.getItems().length);
        assertEquals(true, emptyAllIngredient.isEmpty());

        assertEquals(false, emptyAllIngredient.test(new ItemStack(Items.APPLE)));
        assertEquals(false, emptyAllIngredient.test(new ItemStack(Items.STICK)));

        context.succeed();
    }

    @GameTest(templateNamespace = IngredientTestsImpl.MODID, template = "empty")
    @PrefixGameTestTemplate(false)
    public void testAnyIngredient(GameTestHelper context) {
        Ingredient anyIngredient = DefaultCustomIngredients.any(Ingredient.of(Items.APPLE, Items.CARROT), Ingredient.of(Items.STICK, Items.CARROT));

        assertEquals(4, anyIngredient.getItems().length);
        assertEquals(Items.APPLE, anyIngredient.getItems()[0].getItem());
        assertEquals(Items.CARROT, anyIngredient.getItems()[1].getItem());
        assertEquals(Items.STICK, anyIngredient.getItems()[2].getItem());

        assertEquals(Items.CARROT, anyIngredient.getItems()[3].getItem());
        assertEquals(false, anyIngredient.isEmpty());

        assertEquals(true, anyIngredient.test(new ItemStack(Items.APPLE)));
        assertEquals(true, anyIngredient.test(new ItemStack(Items.CARROT)));
        assertEquals(true, anyIngredient.test(new ItemStack(Items.STICK)));

        context.succeed();
    }

	@GameTest(templateNamespace = IngredientTestsImpl.MODID, template = "empty")
    @PrefixGameTestTemplate(false)
    public void testDifferenceIngredient(GameTestHelper context) {
        Ingredient differenceIngredient = DefaultCustomIngredients.difference(Ingredient.of(Items.APPLE, Items.CARROT), Ingredient.of(Items.STICK, Items.CARROT));

        assertEquals(1, differenceIngredient.getItems().length);
        assertEquals(Items.APPLE, differenceIngredient.getItems()[0].getItem());
        assertEquals(false, differenceIngredient.isEmpty());

        assertEquals(true, differenceIngredient.test(new ItemStack(Items.APPLE)));
        assertEquals(false, differenceIngredient.test(new ItemStack(Items.CARROT)));
        assertEquals(false, differenceIngredient.test(new ItemStack(Items.STICK)));

        context.succeed();
    }

	@GameTest(templateNamespace = IngredientTestsImpl.MODID, template = "empty")
	@PrefixGameTestTemplate(false)
    public void testNbtIngredient(GameTestHelper context) {
        for (boolean strict : List.of(true, false)) {
            CompoundTag undamagedNbt = new CompoundTag();
            undamagedNbt.putInt(ItemStack.TAG_DAMAGE, 0);

            Ingredient nbtIngredient = DefaultCustomIngredients.nbt(Ingredient.of(Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE), undamagedNbt, strict);

            assertEquals(2, nbtIngredient.getItems().length);
            assertEquals(Items.DIAMOND_PICKAXE, nbtIngredient.getItems()[0].getItem());
            assertEquals(Items.NETHERITE_PICKAXE, nbtIngredient.getItems()[1].getItem());
            assertEquals(undamagedNbt, nbtIngredient.getItems()[0].getTag());
            assertEquals(undamagedNbt, nbtIngredient.getItems()[1].getTag());
            assertEquals(false, nbtIngredient.isEmpty());

            // Undamaged is fine
            assertEquals(true, nbtIngredient.test(new ItemStack(Items.DIAMOND_PICKAXE)));
            assertEquals(true, nbtIngredient.test(new ItemStack(Items.NETHERITE_PICKAXE)));

            // Damaged is not fine
            ItemStack damagedDiamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
            damagedDiamondPickaxe.setDamageValue(10);
            assertEquals(false, nbtIngredient.test(damagedDiamondPickaxe));

            // Renamed undamaged is only fine in partial matching
            ItemStack renamedUndamagedDiamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
            renamedUndamagedDiamondPickaxe.setHoverName(Component.literal("Renamed"));
            assertEquals(!strict, nbtIngredient.test(renamedUndamagedDiamondPickaxe));
        }

        // Also test strict null NBT matching
        Ingredient noNbtIngredient = DefaultCustomIngredients.nbt(Ingredient.of(Items.APPLE), null, true);

        assertEquals(1, noNbtIngredient.getItems().length);
        assertEquals(Items.APPLE, noNbtIngredient.getItems()[0].getItem());
        assertEquals(null, noNbtIngredient.getItems()[0].getTag());
        assertEquals(false, noNbtIngredient.isEmpty());

        // No NBT is fine
        assertEquals(true, noNbtIngredient.test(new ItemStack(Items.APPLE)));

        // NBT is not fine
        ItemStack nbtApple = new ItemStack(Items.APPLE);
        nbtApple.setHoverName(Component.literal("Renamed"));
        assertEquals(false, noNbtIngredient.test(nbtApple));

        context.succeed();
    }

    private static <T> void assertEquals(T expected, T actual) {
        if (!Objects.equals(expected, actual)) {
            throw new GameTestAssertException(String.format("assertEquals failed%nexpected: %s%n but was: %s", expected, actual));
        }
    }
}
