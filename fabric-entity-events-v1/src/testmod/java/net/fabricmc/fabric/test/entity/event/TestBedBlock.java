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

package net.fabricmc.fabric.test.entity.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TestBedBlock extends Block {
	private static final VoxelShape SHAPE = box(0, 0, 0, 16, 8, 16);
	public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

	public TestBedBlock(BlockBehaviour.Properties settings) {
		super(settings);
		registerDefaultState(defaultBlockState().setValue(OCCUPIED, false));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return SHAPE;
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (state.getValue(OCCUPIED)) {
			player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
			return InteractionResult.CONSUME;
		}

		if (world.dimensionType().bedWorks()) {
			if (!world.isClientSide) {
				player.startSleepInBed(pos).ifLeft(sleepFailureReason -> {
					Component message = sleepFailureReason.getMessage();

					if (message != null) {
						player.displayClientMessage(message, true);
					}
				});
			}

			return InteractionResult.CONSUME;
		}

		return InteractionResult.PASS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(OCCUPIED);
	}
}
