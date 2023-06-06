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

package net.fabricmc.fabric.test.item.gametest;

import net.fabricmc.fabric.test.item.CustomDamageTest;
import net.fabricmc.fabric.test.item.FabricItemTestsImpl;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(FabricItemTestsImpl.MODID)
public class RecipeGameTest {

    @GameTest(templateNamespace = FabricItemTestsImpl.MODID, template = "empty")
    @PrefixGameTestTemplate(false)
    public void vanillaRemainderTest(GameTestHelper context) {
        Recipe<SimpleContainer> testRecipe = createTestingRecipeInstance();

		SimpleContainer inventory = new SimpleContainer(
            new ItemStack(Items.WATER_BUCKET),
            new ItemStack(Items.DIAMOND));

		NonNullList<ItemStack> remainderList = testRecipe.getRemainingItems(inventory);

        assertStackList(remainderList, "Testing vanilla recipe remainder.",
            new ItemStack(Items.BUCKET),
            ItemStack.EMPTY);

        context.succeed();
    }

    @GameTest(templateNamespace = FabricItemTestsImpl.MODID, template = "empty")
    @PrefixGameTestTemplate(false)
    public void fabricRemainderTest(GameTestHelper context) {
        Recipe<SimpleContainer> testRecipe = createTestingRecipeInstance();

		SimpleContainer inventory = new SimpleContainer(
            new ItemStack(CustomDamageTest.WEIRD_PICK.get()),
            withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK.get()), 10),
            withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK.get()), 31),
            new ItemStack(Items.DIAMOND));

        NonNullList<ItemStack> remainderList = testRecipe.getRemainingItems(inventory);

        assertStackList(remainderList, "Testing fabric recipe remainder.",
            withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK.get()), 1),
            withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK.get()), 11),
            ItemStack.EMPTY,
            ItemStack.EMPTY);

        context.succeed();
    }

    private Recipe<SimpleContainer> createTestingRecipeInstance() {
        return new Recipe<>() {
            @Override
            public boolean matches(SimpleContainer inventory, Level world) {
                return true;
            }

			@Override
			public ItemStack assemble(SimpleContainer p_44001_, RegistryAccess p_267165_) {
				return null;
			}

			@Override
			public boolean canCraftInDimensions(int pWidth, int pHeight) {
				return false;
			}

			@Override
			public ItemStack getResultItem(RegistryAccess p_267052_) {
				return null;
			}

			@Override
			public ResourceLocation getId() {
				return null;
			}

			@Override
			public RecipeSerializer<?> getSerializer() {
				return null;
			}

			@Override
			public RecipeType<?> getType() {
				return null;
			}
		};
    }

    private void assertStackList(NonNullList<ItemStack> stackList, String extraErrorInfo, ItemStack... stacks) {
        for (int i = 0; i < stackList.size(); i++) {
            ItemStack currentStack = stackList.get(i);
            ItemStack expectedStack = stacks[i];

            assertStacks(currentStack, expectedStack, extraErrorInfo);
        }
    }

    static void assertStacks(ItemStack currentStack, ItemStack expectedStack, String extraErrorInfo) {
        if (currentStack.isEmpty() && expectedStack.isEmpty()) {
            return;
        }

        if (!currentStack.is(expectedStack.getItem())) {
            throw new GameTestAssertException("Item stacks dont match. " + extraErrorInfo);
        }

        if (currentStack.getCount() != expectedStack.getCount()) {
            throw new GameTestAssertException("Size doesnt match. " + extraErrorInfo);
        }

        if (!ItemStack.tagMatches(currentStack, expectedStack)) {
            throw new GameTestAssertException("Nbt doesnt match. " + extraErrorInfo);
        }
    }

    static ItemStack withDamage(ItemStack stack, int damage) {
        stack.setDamageValue(damage);
        return stack;
    }
}
