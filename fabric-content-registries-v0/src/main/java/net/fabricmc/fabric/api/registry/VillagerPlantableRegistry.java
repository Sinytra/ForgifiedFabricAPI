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

package net.fabricmc.fabric.api.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Registry of items that farmer villagers can plant on farmland.
 */
public final class VillagerPlantableRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(VillagerPlantableRegistry.class);
	private static final HashMap<Item, BlockState> PLANTABLES = new HashMap<>();
	static {
		register(Items.WHEAT_SEEDS);
		register(Items.CARROT);
		register(Items.POTATO);
		register(Items.BEETROOT_SEEDS);
	}

	private VillagerPlantableRegistry() {
	}

	/**
	 * Registers a BlockItem to be plantable by farmer villagers.
	 * This will use the default state of the associated block.
	 * For the crop to be harvestable, the block should extend CropBlock, so the
	 * farmer can test the {@link CropBlock#isMaxAge(BlockState)} method.
	 * @param item the BlockItem to register
	 */
	public static void register(ItemLike item) {
		Objects.requireNonNull(item.asItem(), "Item cannot be null!");

		if (!(item.asItem() instanceof BlockItem blockItem)) {
			throw new IllegalArgumentException("item is not a BlockItem");
		}

		register(item, blockItem.getBlock().defaultBlockState());
	}

	/**
	 * Register an item with an associated to be plantable by farmer villagers.
	 * For the crop to be harvestable, the block should extend CropBlock, so the
	 * farmer can test the {@link CropBlock#isMaxAge(BlockState)} method.
	 * @param item       the seed item
	 * @param plantState the state that will be planted
	 */
	public static void register(ItemLike item, BlockState plantState) {
		Objects.requireNonNull(item.asItem(), "Item cannot be null!");
		Objects.requireNonNull(plantState, "Plant block state cannot be null!");

		PLANTABLES.put(item.asItem(), plantState);

		if (!(plantState.getBlock() instanceof CropBlock)) {
			LOGGER.info("Registered a block ({}) that does not extend CropBlock, this block will not be villager harvestable by default.", ForgeRegistries.BLOCKS.getKey(plantState.getBlock()));
		}
	}

	/**
	 * Tests if the item is a registered seed item.
	 * @param item the item to test
	 * @return true if the item is registered as a seed
	 */
	public static boolean contains(ItemLike item) {
		Objects.requireNonNull(item.asItem(), "Item cannot be null!");
		return PLANTABLES.containsKey(item.asItem());
	}

	/**
	 * Get the state that is associated with the provided seed item.
	 * @param item the seed item
	 * @return the state associated with the seed item
	 */
	public static BlockState getPlantState(ItemLike item) {
		Objects.requireNonNull(item.asItem(), "Item cannot be null!");
		return PLANTABLES.get(item.asItem());
	}

	/**
	 * Get all currently registered seed items.
	 * @return all currently registered seed items.
	 */
	public static Set<Item> getItems() {
		return Collections.unmodifiableSet(PLANTABLES.keySet());
	}
}
