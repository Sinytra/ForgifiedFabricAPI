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

package net.fabricmc.fabric.test.access;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SignItem;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.test.access.client.BlockEntityRendererTest;

@Mod("fabric_transitive_access_wideners_v1_testmod")
public final class SignBlockEntityTest {
	public static final String MOD_ID = "fabric-transitive-access-wideners-v1-testmod";

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

	public static final RegistryObject<SignBlock> TEST_SIGN = BLOCKS.register("test_sign", () -> new SignBlock(FabricBlockSettings.copy(Blocks.OAK_SIGN), WoodType.OAK) {
		@Override
		public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
			return new TestSign(pos, state);
		}
	});
	public static final RegistryObject<WallSignBlock> TEST_WALL_SIGN = BLOCKS.register("test_wall_sign", () -> new WallSignBlock(FabricBlockSettings.copy(Blocks.OAK_SIGN), WoodType.OAK) {
		@Override
		public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
			return new TestSign(pos, state);
		}
	});
	public static final RegistryObject<SignItem> TEST_SIGN_ITEM = ITEMS.register("test_sign", () -> new SignItem(new Item.Settings(), TEST_SIGN.get(), TEST_WALL_SIGN.get()));
	public static final RegistryObject<BlockEntityType<TestSign>> TEST_SIGN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("test_sign", () -> FabricBlockEntityTypeBuilder.create(TestSign::new, TEST_SIGN.get(), TEST_WALL_SIGN.get()).build());

	public SignBlockEntityTest() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		BLOCKS.register(bus);
		ITEMS.register(bus);
		BLOCK_ENTITY_TYPES.register(bus);
		if (FMLLoader.getDist() == Dist.CLIENT) {
			bus.addListener(BlockEntityRendererTest::onInitializeClient);
		}
	}

	public static class TestSign extends SignBlockEntity {
		public TestSign(BlockPos pos, BlockState state) {
			super(pos, state);
		}

		@Override
		public BlockEntityType<?> getType() {
			return TEST_SIGN_BLOCK_ENTITY.get();
		}
	}
}
