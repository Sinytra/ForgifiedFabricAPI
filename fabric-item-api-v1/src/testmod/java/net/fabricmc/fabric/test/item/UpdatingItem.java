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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class UpdatingItem extends Item implements FabricItem {
    private static final AttributeModifier PLUS_FIVE = new AttributeModifier(
        BASE_ATTACK_DAMAGE_UUID, "updating item", 5, AttributeModifier.Operation.ADDITION);

    private final boolean allowUpdateAnimation;

    public UpdatingItem(boolean allowUpdateAnimation) {
        super(new Properties());
        this.allowUpdateAnimation = allowUpdateAnimation;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong("ticks", tag.getLong("ticks") + 1);
        }
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return allowUpdateAnimation;
    }

    @Override
    public boolean allowContinuingBlockBreaking(Player player, ItemStack oldStack, ItemStack newStack) {
        return true; // set to false and you won't be able to break a block in survival with this item
    }

    // True for 15 seconds every 30 seconds
    private boolean isEnabled(ItemStack stack) {
        return !stack.hasTag() || stack.getTag().getLong("ticks") % 600 < 300;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        // Give + 5 attack damage for 15 seconds every 30 seconds.
        if (slot == EquipmentSlot.MAINHAND && isEnabled(stack)) {
            return ImmutableMultimap.of(Attributes.ATTACK_DAMAGE, PLUS_FIVE);
        } else {
            return ImmutableMultimap.of();
        }
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, BlockState state) {
        // Suitable for everything for 15 seconds every 30 seconds.
        return isEnabled(stack);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isEnabled(stack) ? 20 : super.getDestroySpeed(stack, state);
    }
}
