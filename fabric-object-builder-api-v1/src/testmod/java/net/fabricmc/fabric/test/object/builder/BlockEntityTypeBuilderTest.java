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

package net.fabricmc.fabric.test.object.builder;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockEntityTypeBuilderTest {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ObjectBuilderTestConstants.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ObjectBuilderTestConstants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ObjectBuilderTestConstants.MOD_ID);

    static final RegistryObject<Block> INITIAL_BETRAYAL_BLOCK = registerBlock("initial_betrayal_block", () -> new BetrayalBlock(MaterialColor.COLOR_BLUE));
    static final RegistryObject<Block> ADDED_BETRAYAL_BLOCK = registerBlock("added_betrayal_block", () -> new BetrayalBlock(MaterialColor.COLOR_GREEN));
    static final RegistryObject<Block> FIRST_MULTI_BETRAYAL_BLOCK = registerBlock("first_multi_betrayal_block", () -> new BetrayalBlock(MaterialColor.COLOR_RED));
    static final RegistryObject<Block> SECOND_MULTI_BETRAYAL_BLOCK = registerBlock("second_multi_betrayal_block", () -> new BetrayalBlock(MaterialColor.COLOR_YELLOW));
    public static final RegistryObject<BlockEntityType<?>> BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("betrayal_block", () -> FabricBlockEntityTypeBuilder.create(BetrayalBlockEntity::new, INITIAL_BETRAYAL_BLOCK.get())
        .addBlock(ADDED_BETRAYAL_BLOCK.get())
        .addBlocks(FIRST_MULTI_BETRAYAL_BLOCK.get(), SECOND_MULTI_BETRAYAL_BLOCK.get())
        .build());

    public static void onInitialize(IEventBus bus) {
        BLOCKS.register(bus);
		ITEMS.register(bus);
		BLOCK_ENTITY_TYPES.register(bus);
    }

    private static RegistryObject<Block> registerBlock(String name, Supplier<Block> supplier) {
        RegistryObject<Block> block = BLOCKS.register(name, supplier);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private static class BetrayalBlock extends Block implements EntityBlock {
        private BetrayalBlock(MaterialColor color) {
            super(BlockBehaviour.Properties.copy(Blocks.STONE).color(color));
        }

        @Override
        public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!world.isClientSide()) {
                BlockEntity blockEntity = world.getBlockEntity(pos);

                if (blockEntity == null) {
                    throw new AssertionError("Missing block entity for betrayal block at " + pos);
                } else if (!BLOCK_ENTITY_TYPE.get().equals(blockEntity.getType())) {
                    ResourceLocation id = BlockEntityType.getKey(blockEntity.getType());
                    throw new AssertionError("Incorrect block entity for betrayal block at " + pos + ": " + id);
                }

                Component posText = Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ());
				Component message = Component.translatable("text.fabric_object_builder_api_v1_testmod.block_entity_type_success", posText, BLOCK_ENTITY_TYPE.getId());

                player.displayClientMessage(message, false);
            }

            return InteractionResult.SUCCESS;
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new BetrayalBlockEntity(pos, state);
        }
    }

    private static class BetrayalBlockEntity extends BlockEntity {
        private BetrayalBlockEntity(BlockPos pos, BlockState state) {
            super(BLOCK_ENTITY_TYPE.get(), pos, state);
        }
    }
}
