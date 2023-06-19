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

package net.fabricmc.fabric.test.renderer.simple;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// Need to implement FabricBlock manually because this is a testmod for another Fabric module, otherwise it would be injected.
public final class FrameBlock extends Block implements EntityBlock, FabricBlock {
    public final ResourceLocation id;

    public FrameBlock(ResourceLocation id) {
        super(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).nonOpaque());
        this.id = id;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof FrameBlockEntity frame) {
            ItemStack stack = player.getItemInHand(hand);
            Block handBlock = Block.byItem(stack.getItem());

            @Nullable
            Block currentBlock = frame.getBlock();

            if (stack.isEmpty()) {
                // Try to remove if the stack in hand is empty
                if (currentBlock != null) {
                    if (!world.isClientSide()) {
                        player.getInventory().placeItemBackInInventory(new ItemStack(currentBlock));
                        frame.setBlock(null);
                    }

                    return InteractionResult.sidedSuccess(world.isClientSide());
                }

                return InteractionResult.PASS;
            }

            // getBlockFromItem will return air if we do not have a block item in hand
            if (handBlock == Blocks.AIR) {
                return InteractionResult.FAIL;
            }

            // Do not allow blocks that may have a block entity
            if (handBlock instanceof EntityBlock) {
                return InteractionResult.FAIL;
            }

            stack.shrink(1);

            if (!world.isClientSide()) {
                if (currentBlock != null) {
                    player.getInventory().placeItemBackInInventory(new ItemStack(currentBlock));
                }

                frame.setBlock(handBlock);
            }

            return InteractionResult.sidedSuccess(world.isClientSide());
        }
        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FrameBlockEntity(pos, state);
    }

    // The frames don't look exactly like the block they are mimicking,
    // but the goal here is just to test the behavior with the pillar's connected textures. ;-)
    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        // For this specific block, the render attachment works on both the client and the server, so let's use that.
        if (((RenderAttachedBlockView) renderView).getBlockEntityRenderAttachment(pos) instanceof Block mimickedBlock) {
            return mimickedBlock.defaultBlockState();
        }

        return state;
    }
}
