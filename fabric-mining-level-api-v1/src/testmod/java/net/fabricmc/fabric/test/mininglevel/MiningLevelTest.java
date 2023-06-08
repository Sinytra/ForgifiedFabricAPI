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

package net.fabricmc.fabric.test.mininglevel;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// This test must pass without the tool attribute API present.
// It has its own handlers for mining levels, which might "hide" this module
// not working on its own.
@Mod(MiningLevelTest.MODID)
public final class MiningLevelTest {
	public static final String MODID = "fabric_mining_level_api_v1_testmod";
	private static final Logger LOGGER = LoggerFactory.getLogger(MiningLevelTest.class);

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

	/// Tagged blocks
	// sword + dynamic mining level tag
	public static final RegistryObject<Block> NEEDS_NETHERITE_SWORD = BLOCKS.register("needs_netherite_sword", () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(2, 3).requiresCorrectToolForDrops()));
	public static final RegistryObject<Item> NEEDS_NETHERITE_SWORD_ITEM = blockItem(NEEDS_NETHERITE_SWORD);
	// sword + vanilla mining level tag
	public static final RegistryObject<Block> NEEDS_STONE_SWORD = BLOCKS.register("needs_stone_sword", () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(2, 3).requiresCorrectToolForDrops()));
	public static final RegistryObject<Item> NEEDS_STONE_SWORD_ITEM = blockItem(NEEDS_STONE_SWORD);
	// any sword
	public static final RegistryObject<Block> NEEDS_ANY_SWORD = BLOCKS.register("needs_any_sword", () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(2, 3).requiresCorrectToolForDrops()));
	public static final RegistryObject<Item> NEEDS_ANY_SWORD_ITEM = blockItem(NEEDS_ANY_SWORD);
	// shears
	public static final RegistryObject<Block> NEEDS_SHEARS = BLOCKS.register("needs_shears", () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(2, 3).requiresCorrectToolForDrops()));
	public static final RegistryObject<Item> NEEDS_SHEARS_ITEM = blockItem(NEEDS_SHEARS);
	// vanilla mineable tag + dynamic mining level tag
	public static final RegistryObject<Block> NEEDS_NETHERITE_PICKAXE = BLOCKS.register("needs_netherite_pickaxe", () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(2, 3).requiresCorrectToolForDrops()));
	public static final RegistryObject<Item> NEEDS_NETHERITE_PICKAXE_ITEM = blockItem(NEEDS_NETHERITE_PICKAXE);
	// vanilla mineable tag, requires tool (this type of block doesn't exist in vanilla)
	public static final RegistryObject<Block> NEEDS_AXE = BLOCKS.register("needs_axe", () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(2, 3).requiresCorrectToolForDrops()));
	public static final RegistryObject<Item> NEEDS_AXE_ITEM = blockItem(NEEDS_AXE);
	// vanilla mineable tag, requires tool (this type of block doesn't exist in vanilla)
	public static final RegistryObject<Block> NEEDS_HOE = BLOCKS.register("needs_hoe", () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(2, 3).requiresCorrectToolForDrops()));
	public static final RegistryObject<Item> NEEDS_HOE_ITEM = blockItem(NEEDS_HOE);
	// vanilla mineable tag, requires tool (this type of block doesn't exist in vanilla)
	public static final RegistryObject<Block> NEEDS_SHOVEL = BLOCKS.register("needs_shovel", () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(2, 3).requiresCorrectToolForDrops()));
	public static final RegistryObject<Item> NEEDS_SHOVEL_ITEM = blockItem(NEEDS_SHOVEL);

	public MiningLevelTest() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		BLOCKS.register(bus);
		ITEMS.register(bus);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> test());
	}

	private static RegistryObject<Item> blockItem(RegistryObject<Block> block) {
		return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}

	private static void test() {
		List<AssertionError> errors = new ArrayList<>();
		test(errors, () -> checkMiningLevel(NEEDS_NETHERITE_SWORD.get(), List.of(Items.NETHERITE_SWORD), List.of(Items.NETHERITE_PICKAXE, Items.STONE_SWORD)));
		test(errors, () -> checkMiningLevel(NEEDS_STONE_SWORD.get(), List.of(Items.STONE_SWORD, Items.IRON_SWORD), List.of(Items.STONE_PICKAXE, Items.WOODEN_SWORD)));
		test(errors, () -> checkMiningLevel(NEEDS_ANY_SWORD.get(), List.of(Items.WOODEN_SWORD), List.of()));
		test(errors, () -> checkMiningLevel(NEEDS_SHEARS.get(), List.of(Items.SHEARS), List.of()));
		test(errors, () -> checkMiningLevel(NEEDS_NETHERITE_PICKAXE.get(), List.of(Items.NETHERITE_PICKAXE), List.of(Items.DIAMOND_PICKAXE, Items.NETHERITE_AXE)));
		test(errors, () -> checkMiningLevel(Blocks.STONE, List.of(Items.WOODEN_PICKAXE), List.of(Items.STICK)));
		test(errors, () -> checkMiningLevel(NEEDS_AXE.get(), List.of(Items.WOODEN_AXE), List.of(Items.STICK)));
		test(errors, () -> checkMiningLevel(NEEDS_HOE.get(), List.of(Items.WOODEN_HOE), List.of(Items.STICK)));
		test(errors, () -> checkMiningLevel(NEEDS_SHOVEL.get(), List.of(Items.WOODEN_SHOVEL), List.of(Items.STICK)));

		if (errors.isEmpty()) {
			LOGGER.info("Mining level tests passed!");
		} else {
			AssertionError error = new AssertionError("Mining level tests failed!");
			errors.forEach(error::addSuppressed);
			throw error;
		}
	}

	private static void test(List<AssertionError> errors, Runnable runnable) {
		try {
			runnable.run();
		} catch (AssertionError e) {
			errors.add(e);
		}
	}

	private static void checkMiningLevel(Block block, List<Item> successfulItems, List<Item> failingItems) {
		BlockState state = block.defaultBlockState();

		for (Item success : successfulItems) {
			ItemStack successStack = new ItemStack(success);

			if (!successStack.isCorrectToolForDrops(state)) {
				throw new AssertionError(success + " is not suitable for " + block);
			}

			if (successStack.getDestroySpeed(state) == 1f) {
				throw new AssertionError(success + " returns default mining speed for " + block);
			}
		}

		for (Item failing : failingItems) {
			if (new ItemStack(failing).isCorrectToolForDrops(state)) {
				throw new AssertionError(failing + " is suitable for " + block);
			}
		}
	}
}
