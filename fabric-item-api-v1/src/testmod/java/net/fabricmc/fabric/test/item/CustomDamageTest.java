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

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CustomDamageTest {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FabricItemTestsImpl.MODID);
    public static final RegistryObject<Item> WEIRD_PICK = ITEMS.register("weird_pickaxe", WeirdPick::new);

    public static void onInitialize(IEventBus bus) {
        bus.addListener(CustomDamageTest::onCommonSetup);
		ITEMS.register(bus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        FuelRegistry.INSTANCE.add(WEIRD_PICK.get(), 200);
		FabricBrewingRecipeRegistry.registerPotionRecipe(Potions.WATER, Ingredient.of(WEIRD_PICK.get()), Potions.AWKWARD);
    }

    public static final CustomDamageHandler WEIRD_DAMAGE_HANDLER = (stack, amount, entity, breakCallback) -> {
        // If sneaking, apply all damage to vanilla. Otherwise, increment a tag on the stack by one and don't apply any damage
        if (entity.isShiftKeyDown()) {
            return amount;
        } else {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("weird", tag.getInt("weird") + 1);
            return 0;
        }
    };

    public static class WeirdPick extends PickaxeItem implements FabricItem {
        protected WeirdPick() {
            super(Tiers.GOLD, 1, -2.8F, new FabricItemSettings().customDamage(WEIRD_DAMAGE_HANDLER));
        }

        @Override
        public Component getName(ItemStack stack) {
            int v = stack.getOrCreateTag().getInt("weird");
            return super.getName(stack).copy().append(" (Weird Value: " + v + ")");
        }

        @Override
        public ItemStack getRecipeRemainder(ItemStack stack) {
            if (stack.getDamageValue() < stack.getMaxDamage() - 1) {
                ItemStack moreDamaged = stack.copy();
                moreDamaged.setCount(1);
                moreDamaged.setDamageValue(stack.getDamageValue() + 1);
                return moreDamaged;
            }

            return ItemStack.EMPTY;
        }
    }
}
