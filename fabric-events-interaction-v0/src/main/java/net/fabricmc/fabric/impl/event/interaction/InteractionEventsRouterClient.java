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

package net.fabricmc.fabric.impl.event.interaction;

import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class InteractionEventsRouterClient {

	public static void onInitializeClient() {
		ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
			if (result instanceof BlockHitResult blockHitResult && result.getType() != HitResult.Type.MISS) {
				BlockGetter view = player.getLevel();
				BlockPos pos = blockHitResult.getBlockPos();
				BlockState state = view.getBlockState(pos);

				if (state.getBlock() instanceof BlockPickInteractionAware aware) {
					return aware.getPickedStack(state, view, pos, player, result);
				}
			} else if (result instanceof EntityHitResult entityHitResult) {
				Entity entity = entityHitResult.getEntity();

				if (entity instanceof EntityPickInteractionAware aware) {
					return aware.getPickedStack(player, result);
				}
			}

			return ItemStack.EMPTY;
		});
	}
}
