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

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(IngredientTestsImpl.MODID)
public class ShapelessRecipeMatchTests {
    /**
     * The recipe requires at least one undamaged pickaxe.
     */
    @GameTest(templateNamespace = IngredientTestsImpl.MODID, template = "empty")
    @PrefixGameTestTemplate(false)
    public void testShapelessMatch(GameTestHelper context) {
        ResourceLocation recipeId = new ResourceLocation(IngredientTestsImpl.MODID, "test_shapeless_match");
        ShapelessRecipe recipe = (ShapelessRecipe) context.getLevel().getRecipeManager().byKey(recipeId).get();

        ItemStack undamagedPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        ItemStack damagedPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        damagedPickaxe.setDamageValue(100);

        CraftingContainer craftingInv = new CraftingContainer(new AbstractContainerMenu(null, 0) {
            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return false;
            }
        }, 3, 3);

        // Test that damaged only doesn't work
        for (int i = 0; i < 9; ++i) {
            craftingInv.setItem(i, damagedPickaxe);
        }

        if (recipe.matches(craftingInv, context.getLevel())) {
            throw new GameTestAssertException("Recipe should not match with only damaged pickaxes");
        }

        craftingInv.setItem(1, undamagedPickaxe);

        if (!recipe.matches(craftingInv, context.getLevel())) {
            throw new GameTestAssertException("Recipe should match with at least one undamaged pickaxe");
        }

        context.succeed();
    }
}
