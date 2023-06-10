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

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TealSignTest {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ObjectBuilderTestConstants.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ObjectBuilderTestConstants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ObjectBuilderTestConstants.MOD_ID);

    public static final BlockSetType TEAL_BLOCK_SET_TYPE = BlockSetTypeRegistry.registerWood(ObjectBuilderTestConstants.id("teal"));
    public static final WoodType TEAL_WOOD_TYPE = WoodTypeRegistry.register(ObjectBuilderTestConstants.id("teal"), TEAL_BLOCK_SET_TYPE);
    public static final RegistryObject<StandingSignBlock> TEAL_SIGN = BLOCKS.register("teal_sign", () -> new StandingSignBlock(FabricBlockSettings.copy(Blocks.OAK_SIGN), TEAL_WOOD_TYPE) {
        @Override
        public TealSign newBlockEntity(BlockPos pos, BlockState state) {
            return new TealSign(pos, state);
        }
    });
    public static final RegistryObject<WallSignBlock> TEAL_WALL_SIGN = BLOCKS.register("teal_wall_sign", () -> new WallSignBlock(FabricBlockSettings.copy(Blocks.OAK_SIGN), TEAL_WOOD_TYPE) {
        @Override
        public TealSign newBlockEntity(BlockPos pos, BlockState state) {
            return new TealSign(pos, state);
        }
    });
    public static final RegistryObject<CeilingHangingSignBlock> TEAL_HANGING_SIGN = BLOCKS.register("teal_hanging_sign", () -> new CeilingHangingSignBlock(FabricBlockSettings.copy(Blocks.OAK_HANGING_SIGN), TEAL_WOOD_TYPE) {
        @Override
        public TealHangingSign newBlockEntity(BlockPos pos, BlockState state) {
            return new TealHangingSign(pos, state);
        }
    });
    public static final RegistryObject<WallHangingSignBlock> TEAL_WALL_HANGING_SIGN = BLOCKS.register("teal_wall_hanging_sign", () -> new WallHangingSignBlock(FabricBlockSettings.copy(Blocks.OAK_HANGING_SIGN), TEAL_WOOD_TYPE) {
        @Override
        public TealHangingSign newBlockEntity(BlockPos pos, BlockState state) {
            return new TealHangingSign(pos, state);
        }
    });
    public static final RegistryObject<SignItem> TEAL_SIGN_ITEM = ITEMS.register("teal_sign", () -> new SignItem(new Item.Properties(), TEAL_SIGN.get(), TEAL_WALL_SIGN.get()));
    public static final RegistryObject<HangingSignItem> TEAL_HANGING_SIGN_ITEM = ITEMS.register("teal_hanging_sign", () -> new HangingSignItem(TEAL_HANGING_SIGN.get(), TEAL_WALL_HANGING_SIGN.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<TealSign>> TEST_SIGN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("teal_sign", () -> FabricBlockEntityTypeBuilder.create(TealSign::new, TEAL_SIGN.get(), TEAL_WALL_SIGN.get()).build());
    public static final RegistryObject<BlockEntityType<TealHangingSign>> TEST_HANGING_SIGN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("teal_hanging_sign", () -> FabricBlockEntityTypeBuilder.create(TealHangingSign::new, TEAL_HANGING_SIGN.get(), TEAL_WALL_HANGING_SIGN.get()).build());

    public static void onInitialize(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    public static class TealSign extends SignBlockEntity {
        public TealSign(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public BlockEntityType<?> getType() {
            return TEST_SIGN_BLOCK_ENTITY.get();
        }
    }

    public static class TealHangingSign extends HangingSignBlockEntity {
        public TealHangingSign(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public BlockEntityType<?> getType() {
            return TEST_HANGING_SIGN_BLOCK_ENTITY.get();
        }
    }
}
