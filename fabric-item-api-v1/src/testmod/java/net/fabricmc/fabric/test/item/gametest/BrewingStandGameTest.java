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
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Objects;

@GameTestHolder(FabricItemTestsImpl.MODID)
public class BrewingStandGameTest {
    private static final int BREWING_TIME = 800;
    private static final BlockPos POS = new BlockPos(0, 1, 0);

    @GameTest(templateNamespace = FabricItemTestsImpl.MODID, template = "empty")
    @PrefixGameTestTemplate(false)
    public void basicBrewing(GameTestHelper context) {
        context.setBlock(POS, Blocks.BREWING_STAND);
        BrewingStandBlockEntity blockEntity = (BrewingStandBlockEntity) Objects.requireNonNull(context.getBlockEntity(POS));

        loadFuel(blockEntity, context);

        prepareForBrewing(blockEntity, new ItemStack(Items.NETHER_WART, 8),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));

        brew(blockEntity, context);
        assertInventory(blockEntity, "Testing vanilla brewing.",
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            new ItemStack(Items.NETHER_WART, 7),
            ItemStack.EMPTY);

        context.succeed();
    }

    @GameTest(templateNamespace = FabricItemTestsImpl.MODID, template = "empty")
    @PrefixGameTestTemplate(false)
    public void vanillaRemainderTest(GameTestHelper context) {
        context.setBlock(POS, Blocks.BREWING_STAND);
        BrewingStandBlockEntity blockEntity = (BrewingStandBlockEntity) Objects.requireNonNull(context.getBlockEntity(POS));

        loadFuel(blockEntity, context);

        prepareForBrewing(blockEntity, new ItemStack(Items.DRAGON_BREATH),
            PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.AWKWARD));

        brew(blockEntity, context);
        assertInventory(blockEntity, "Testing vanilla brewing recipe remainder.",
            PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.AWKWARD),
            new ItemStack(Items.GLASS_BOTTLE),
            ItemStack.EMPTY);

        context.succeed();
    }

    //@GameTest(templateName = EMPTY_STRUCTURE)
    // Skip see: https://github.com/FabricMC/fabric/pull/2874
    public void fabricRemainderTest(GameTestHelper context) {
        context.setBlock(POS, Blocks.BREWING_STAND);
        BrewingStandBlockEntity blockEntity = (BrewingStandBlockEntity) Objects.requireNonNull(context.getBlockEntity(POS));

        loadFuel(blockEntity, context);

        prepareForBrewing(blockEntity, new ItemStack(CustomDamageTest.WEIRD_PICK.get()),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));

        brew(blockEntity, context);
        assertInventory(blockEntity, "Testing fabric brewing recipe remainder.",
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK.get()), 1),
            ItemStack.EMPTY);

        prepareForBrewing(blockEntity, RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK.get()), 10),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));

        brew(blockEntity, context);
        assertInventory(blockEntity, "Testing fabric brewing recipe remainder.",
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK.get()), 11),
            ItemStack.EMPTY);

        prepareForBrewing(blockEntity, RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK.get()), 31),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));

        brew(blockEntity, context);
        assertInventory(blockEntity, "Testing fabric brewing recipe remainder.",
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
            ItemStack.EMPTY,
            ItemStack.EMPTY);

        context.succeed();
    }

    private void prepareForBrewing(BrewingStandBlockEntity blockEntity, ItemStack ingredient, ItemStack potion) {
        blockEntity.setItem(0, potion.copy());
        blockEntity.setItem(1, potion.copy());
        blockEntity.setItem(2, potion.copy());
        blockEntity.setItem(3, ingredient);
    }

    private void assertInventory(BrewingStandBlockEntity blockEntity, String extraErrorInfo, ItemStack... stacks) {
        for (int i = 0; i < stacks.length; i++) {
            ItemStack currentStack = blockEntity.getItem(i);
            ItemStack expectedStack = stacks[i];

            RecipeGameTest.assertStacks(currentStack, expectedStack, extraErrorInfo);
        }
    }

    private void loadFuel(BrewingStandBlockEntity blockEntity, GameTestHelper context) {
        blockEntity.setItem(4, new ItemStack(Items.BLAZE_POWDER));
        BrewingStandBlockEntity.serverTick(context.getLevel(), POS, context.getBlockState(POS), blockEntity);
    }

    private void brew(BrewingStandBlockEntity blockEntity, GameTestHelper context) {
        for (int i = 0; i < BREWING_TIME; i++) {
            BrewingStandBlockEntity.serverTick(context.getLevel(), POS, context.getBlockState(POS), blockEntity);
        }
    }
}
