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

package net.fabricmc.fabric.mixin.mininglevel;

import net.fabricmc.fabric.impl.mininglevel.MiningLevelManagerImpl;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DiggerItem.class)
abstract class MiningToolItemMixin {
    @Inject(
        method = "isCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DiggerItem;getTier()Lnet/minecraft/world/item/Tier;", ordinal = 0),
        cancellable = true
    )
    private void fabric$onIsSuitableFor(BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (((DiggerItem) (Object) this).getTier().getLevel() < MiningLevelManagerImpl.getRequiredFabricMiningLevel(state)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "isCorrectToolForDrops(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DiggerItem;getTier()Lnet/minecraft/world/item/Tier;"), cancellable = true)
    private void fabric$onForgeIsSuitableFor(ItemStack stack, BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (((DiggerItem) (Object) this).getTier().getLevel() < MiningLevelManagerImpl.getRequiredFabricMiningLevel(state)) {
            info.setReturnValue(false);
        }
    }
}
