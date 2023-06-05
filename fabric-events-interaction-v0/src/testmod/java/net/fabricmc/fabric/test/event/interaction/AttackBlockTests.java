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

package net.fabricmc.fabric.test.event.interaction;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttackBlockTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttackBlockTests.class);

	public static void onInitialize() {
		AttackBlockCallback.EVENT.register((player, world, hand, pos, side) -> {
			LOGGER.info("AttackBlockCallback: before chest/lava hook (client-side = %s)".formatted(world.isClientSide));
			return InteractionResult.PASS;
		});
		// If a chest is attacked and the player holds a lava bucket, delete it!
		AttackBlockCallback.EVENT.register((player, world, hand, pos, side) -> {
			if (!player.isSpectator() && world.mayInteract(player, pos)) {
				if (world.getBlockState(pos).is(Blocks.CHEST)) {
					if (player.getItemInHand(hand).is(Items.LAVA_BUCKET)) {
						world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
						return InteractionResult.sidedSuccess(world.isClientSide);
					}
				}
			}

			return InteractionResult.PASS;
		});
		AttackBlockCallback.EVENT.register((player, world, hand, pos, side) -> {
			LOGGER.info("AttackBlockCallback: after chest/lava hook (client-side = %s)".formatted(world.isClientSide));
			return InteractionResult.PASS;
		});
	}
}
