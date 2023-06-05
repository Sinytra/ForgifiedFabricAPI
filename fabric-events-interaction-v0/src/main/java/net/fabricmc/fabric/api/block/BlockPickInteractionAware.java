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

package net.fabricmc.fabric.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

/**
 * Convenience interface for blocks that want more stack picking context than what
 * {@link net.minecraft.world.level.block.Block#getCloneItemStack(BlockGetter, BlockPos, BlockState)} provides.
 *
 * <p>The hit result is guaranteed to be a {@link net.minecraft.world.phys.BlockHitResult} that did not miss.
 */
public interface BlockPickInteractionAware {
	ItemStack getPickedStack(BlockState state, BlockGetter view, BlockPos pos, Player player, HitResult result);
}
