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

package net.fabricmc.fabric.impl.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.impl.client.item.ItemApiClientEventHooks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@SuppressWarnings("unused")
public final class FabricItemImplHooks {

    public static ItemStack getCraftingRemainingItem(FabricItem item, ItemStack stack) {
        ItemStack fabricRemainder = FabricItemInternals.nonRecursiveApiCall(() -> item.getRecipeRemainder(stack));
        if (!fabricRemainder.isEmpty()) {
            return fabricRemainder;
        }
        return null;
    }

    public static boolean hasCraftingRemainingItem(FabricItem item, ItemStack stack) {
        return !FabricItemInternals.nonRecursiveApiCall(() -> item.getRecipeRemainder(stack)).isEmpty();
    }

    public static Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(FabricItem item, EquipmentSlot slot, ItemStack stack) {
        return FabricItemInternals.nonRecursiveApiCall(() -> item.getAttributeModifiers(stack, slot));
    }

    public static boolean isCorrectToolForDrops(FabricItem item, ItemStack stack, BlockState state) {
        return FabricItemInternals.nonRecursiveApiCall(() -> item.isSuitableFor(stack, state));
    }

    public static boolean shouldCauseReequipAnimation(FabricItem item, ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        PlayerEntity player = ItemApiClientEventHooks.getClientPlayerSafely();
        Hand hand = oldStack == player.getMainHandStack() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        return FabricItemInternals.nonRecursiveApiCall(() -> item.allowNbtUpdateAnimation(player, hand, oldStack, newStack));
    }

    public static boolean shouldCauseBlockBreakReset(boolean original, FabricItem item, ItemStack oldStack, ItemStack newStack) {
        return original && FabricItemInternals.nonRecursiveApiCall(() -> !item.allowContinuingBlockBreaking(ItemApiClientEventHooks.getClientPlayerSafely(), oldStack, newStack));
    }

    private FabricItemImplHooks() {}
}
