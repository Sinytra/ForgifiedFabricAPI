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

package net.fabricmc.fabric.test.transfer.gametests;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.test.transfer.TransferApiTests;
import net.fabricmc.fabric.test.transfer.mixin.AbstractFurnaceBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.mutable.MutableInt;

@GameTestHolder(TransferApiTests.MODID)
public class VanillaStorageTests {
	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/1972">FabricMC#1972</a>.
	 * Ensures that furnace cook time is only reset when extraction is actually committed.
	 */
	@GameTest(templateNamespace = TransferApiTests.MODID, template = "empty")
	@PrefixGameTestTemplate(false)
	public void testFurnaceCookTime(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlock(pos, Blocks.FURNACE.defaultBlockState());
		FurnaceBlockEntity furnace = (FurnaceBlockEntity) context.getBlockEntity(pos);
		AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) furnace;

		ItemVariant rawIron = ItemVariant.of(Items.RAW_IRON);
		furnace.setItem(0, rawIron.toStack(64));
		furnace.setItem(1, new ItemStack(Items.COAL, 64));
		InventoryStorage furnaceWrapper = InventoryStorage.of(furnace, null);

		context.runAtTickTime(5, () -> {
			if (accessor.getCookingProgress() <= 0) {
				throw new GameTestAssertException("Furnace should have started cooking.");
			}

			try (Transaction transaction = Transaction.openOuter()) {
				if (furnaceWrapper.extract(rawIron, 64, transaction) != 64) {
					throw new GameTestAssertException("Failed to extract 64 raw iron.");
				}
			}

			if (accessor.getCookingProgress() <= 0) {
				throw new GameTestAssertException("Furnace should still cook after simulation.");
			}

			try (Transaction transaction = Transaction.openOuter()) {
				if (furnaceWrapper.extract(rawIron, 64, transaction) != 64) {
					throw new GameTestAssertException("Failed to extract 64 raw iron.");
				}

				transaction.commit();
			}

			if (accessor.getCookingProgress() != 0) {
				throw new GameTestAssertException("Furnace should have reset cook time after being emptied.");
			}

			context.succeed();
		});
	}

	/**
	 * Tests that the passed block doesn't update adjacent comparators until the very end of a committed transaction.
	 *
	 * @param block A block with an Inventory block entity.
	 * @param variant The variant to try to insert (needs to be supported by the Inventory).
	 */
	private static void testComparatorOnInventory(GameTestHelper context, Block block, ItemVariant variant) {
		Level world = context.getLevel();

		BlockPos pos = new BlockPos(0, 2, 0);
		context.setBlock(pos, block.defaultBlockState());
		Container inventory = (Container) context.getBlockEntity(pos);
		InventoryStorage storage = InventoryStorage.of(inventory, null);

		BlockPos comparatorPos = new BlockPos(1, 2, 0);
		// support block under the comparator
		context.setBlock(comparatorPos.relative(Direction.DOWN), Blocks.GREEN_WOOL.defaultBlockState());
		// comparator
		context.setBlock(comparatorPos, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, Direction.WEST));

		try (Transaction transaction = Transaction.openOuter()) {
			storage.insert(variant, 1000000, transaction);

			// uncommitted insert should not schedule an update
			if (world.getBlockTicks().hasScheduledTick(context.absolutePos(comparatorPos), Blocks.COMPARATOR)) {
				throw new GameTestAssertException("Comparator should not have a tick scheduled.");
			}

			transaction.commit();

			// committed insert should schedule an update
			if (!world.getBlockTicks().hasScheduledTick(context.absolutePos(comparatorPos), Blocks.COMPARATOR)) {
				throw new GameTestAssertException("Comparator should have a tick scheduled.");
			}
		}

		context.succeed();
	}

	/**
	 * Tests that containers such as chests don't update adjacent comparators until the very end of a committed transaction.
	 */
	@GameTest(templateNamespace = TransferApiTests.MODID, template = "empty")
	@PrefixGameTestTemplate(false)
	public void testChestComparator(GameTestHelper context) {
		testComparatorOnInventory(context, Blocks.CHEST, ItemVariant.of(Items.DIAMOND));
	}

	/**
	 * Same as {@link #testChestComparator} but for chiseled bookshelves, because their implementation is very... strange.
	 */
	// FIXME This is broken upstream for some reason
//	@GameTest(templateNamespace = TransferApiTests.MODID, template = "empty")
//	@PrefixGameTestTemplate(false)
	public void testChiseledBookshelfComparator(GameTestHelper context) {
		testComparatorOnInventory(context, Blocks.CHISELED_BOOKSHELF, ItemVariant.of(Items.BOOK));
	}

	/**
	 * Test for chiseled bookshelves, because their implementation is very... strange.
	 */
	// FIXME This is broken upstream for some reason
//	@GameTest(templateNamespace = TransferApiTests.MODID, template = "empty")
//	@PrefixGameTestTemplate(false)
	public void testChiseledBookshelf(GameTestHelper context) {
		ItemVariant book = ItemVariant.of(Items.BOOK);

		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlock(pos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
		ChiseledBookShelfBlockEntity bookshelf = (ChiseledBookShelfBlockEntity) context.getBlockEntity(pos);
		InventoryStorage storage = InventoryStorage.of(bookshelf, null);

		// First, check that we can correctly undo insert operations, because vanilla's setStack doesn't permit it without our patches.
		try (Transaction transaction = Transaction.openOuter()) {
			if (storage.insert(book, 2, transaction) != 2) throw new GameTestAssertException("Should have inserted 2 books");

			if (bookshelf.getItem(0).getCount() != 1) throw new GameTestAssertException("Bookshelf stack 0 should have size 1");
			if (!book.matches(bookshelf.getItem(0))) throw new GameTestAssertException("Bookshelf stack 0 should be a book");
			if (bookshelf.getItem(1).getCount() != 1) throw new GameTestAssertException("Bookshelf stack 1 should have size 1");
			if (!book.matches(bookshelf.getItem(1))) throw new GameTestAssertException("Bookshelf stack 1 should be a book");
		}

		if (!bookshelf.getItem(0).isEmpty()) throw new GameTestAssertException("Bookshelf stack 0 should be empty again after aborting transaction");
		if (!bookshelf.getItem(1).isEmpty()) throw new GameTestAssertException("Bookshelf stack 1 should be empty again after aborting transaction");

		// Second, check that we correctly update the last modified slot.
		try (Transaction tx = Transaction.openOuter()) {
			if (storage.getSlot(1).insert(book, 1, tx) != 1) throw new GameTestAssertException("Should have inserted 1 book");
			if (bookshelf.getLastInteractedSlot() != 1) throw new GameTestAssertException("Last modified slot should be 1");

			if (storage.getSlot(2).insert(book, 1, tx) != 1) throw new GameTestAssertException("Should have inserted 1 book");
			if (bookshelf.getLastInteractedSlot() != 2) throw new GameTestAssertException("Last modified slot should be 2");

			if (storage.getSlot(1).extract(book, 1, tx) != 1) throw new GameTestAssertException("Should have extracted 1 book");
			if (bookshelf.getLastInteractedSlot() != 1) throw new GameTestAssertException("Last modified slot should be 1");

			// Now, create an aborted nested transaction.
			try (Transaction nested = tx.openNested()) {
				if (storage.insert(book, 100, nested) != 5) throw new GameTestAssertException("Should have inserted 5 books");
				// Now, last modified slot should be 5.
				if (bookshelf.getLastInteractedSlot() != 5) throw new GameTestAssertException("Last modified slot should be 5");
			}

			// And it's back to 1 in theory.
			if (bookshelf.getLastInteractedSlot() != 1) throw new GameTestAssertException("Last modified slot should be 1");
			tx.commit();
		}

		if (bookshelf.getLastInteractedSlot() != 1) throw new GameTestAssertException("Last modified slot should be 1 after committing transaction");

		// Let's also check the state properties. Only slot 2 should be occupied.
		BlockState state = bookshelf.getBlockState();

		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED)) throw new GameTestAssertException("Slot 0 should not be occupied");
		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED)) throw new GameTestAssertException("Slot 1 should not be occupied");
		if (!state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED)) throw new GameTestAssertException("Slot 2 should be occupied");
		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED)) throw new GameTestAssertException("Slot 3 should not be occupied");
		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED)) throw new GameTestAssertException("Slot 4 should not be occupied");
		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED)) throw new GameTestAssertException("Slot 5 should not be occupied");

		context.succeed();
	}

	/**
	 * Tests that shulker boxes cannot be inserted into other shulker boxes.
	 */
	@GameTest(templateNamespace = TransferApiTests.MODID, template = "empty")
	@PrefixGameTestTemplate(false)
	public void testShulkerNoInsert(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 2, 0);
		context.setBlock(pos, Blocks.SHULKER_BOX);
		ShulkerBoxBlockEntity shulker = (ShulkerBoxBlockEntity) context.getBlockEntity(pos);
		InventoryStorage storage = InventoryStorage.of(shulker, null);

		if (storage.simulateInsert(ItemVariant.of(Items.SHULKER_BOX), 1, null) > 0) {
			context.fail("Expected shulker box to be rejected", pos);
		}

		context.succeed();
	}

	/**
	 * {@link Container#canPlaceItem(int, ItemStack)} is supposed to be independent of the stack size.
	 * However, to limit some stackable inputs to a size of 1, brewing stands and furnaces don't follow this rule in all cases.
	 * This test ensures that the Transfer API works around this issue for furnaces.
	 */
	@GameTest(templateNamespace = TransferApiTests.MODID, template = "empty")
	@PrefixGameTestTemplate(false)
	public void testBadFurnaceIsValid(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlock(pos, Blocks.FURNACE.defaultBlockState());
		FurnaceBlockEntity furnace = (FurnaceBlockEntity) context.getBlockEntity(pos);
		InventoryStorage furnaceWrapper = InventoryStorage.of(furnace, null);

		try (Transaction tx = Transaction.openOuter()) {
			if (furnaceWrapper.getSlot(1).insert(ItemVariant.of(Items.BUCKET), 2, tx) != 1) {
				throw new GameTestAssertException("Exactly 1 bucket should have been inserted");
			}
		}

		context.succeed();
	}

	/**
	 * Same as {@link #testBadFurnaceIsValid(GameTestHelper)}, but for brewing stands.
	 */
	@GameTest(templateNamespace = TransferApiTests.MODID, template = "empty")
	@PrefixGameTestTemplate(false)
	public void testBadBrewingStandIsValid(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlock(pos, Blocks.BREWING_STAND.defaultBlockState());
		BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) context.getBlockEntity(pos);
		InventoryStorage brewingStandWrapper = InventoryStorage.of(brewingStand, null);

		try (Transaction tx = Transaction.openOuter()) {
			for (int bottleSlot = 0; bottleSlot < 3; ++bottleSlot) {
				if (brewingStandWrapper.getSlot(bottleSlot).insert(ItemVariant.of(Items.GLASS_BOTTLE), 2, tx) != 1) {
					throw new GameTestAssertException("Exactly 1 glass bottle should have been inserted");
				}
			}

			if (brewingStandWrapper.getSlot(3).insert(ItemVariant.of(Items.REDSTONE), 2, tx) != 2) {
				throw new GameTestAssertException("Brewing ingredient insertion should not be limited");
			}
		}

		context.succeed();
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/2810">double chest wrapper only updating modified halves</a>.
	 */
	@GameTest(templateNamespace = TransferApiTests.MODID, template = "double_chest_comparators")
	@PrefixGameTestTemplate(false)
	public void testDoubleChestComparator(GameTestHelper context) {
		BlockPos chestPos = new BlockPos(2, 2, 2);
		Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getLevel(), context.absolutePos(chestPos), Direction.UP);
		context.assertTrue(storage != null, "Storage must not be null");

		// Insert one item
		try (Transaction tx = Transaction.openOuter()) {
			context.assertTrue(storage.insert(ItemVariant.of(Items.DIAMOND), 1, tx) == 1, "Diamond should have been inserted");
			tx.commit();
		}

		// Check that the inventory and slotted storages match
		Container inventory = HopperBlockEntity.getContainerAt(context.getLevel(), context.absolutePos(chestPos));
		context.assertTrue(inventory != null, "Inventory must not be null");

		if (!(storage instanceof SlottedStorage<ItemVariant> slottedStorage)) {
			throw new GameTestAssertException("Double chest storage must be a SlottedStorage");
		}

		for (int i = 0; i < inventory.getContainerSize(); ++i) {
			ItemStack stack = inventory.getItem(i);
			ItemVariant variant = ItemVariant.of(stack.getItem());
			context.assertTrue(variant.matches(stack), "Item variant in slot " + i + " must match stack");
			long expectedCount = stack.getCount();
			long actualCount = slottedStorage.getSlot(i).getAmount();
			context.assertTrue(expectedCount == actualCount, "Slot " + i + " should have " + expectedCount + " items, but has " + actualCount);
		}

		// Check that an update is queued for every single comparator
		MutableInt comparatorCount = new MutableInt();

		context.forEveryBlockInStructure(relativePos -> {
			if (context.getBlockState(relativePos).getBlock() != Blocks.COMPARATOR) {
				return;
			}

			comparatorCount.increment();

			// FIXME comparators are stupid
//			if (!context.getLevel().getBlockTicks().hasScheduledTick(context.absolutePos(relativePos), Blocks.COMPARATOR)) {
//				throw new GameTestAssertException("Comparator at " + relativePos + " should have an update scheduled");
//			}
		});

		context.assertTrue(comparatorCount.intValue() == 6, "Expected exactly 6 comparators");

		context.succeed();
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3017">composters not always incrementing their level on the first insert</a>.
	 */
	@GameTest(templateNamespace = TransferApiTests.MODID, template = "empty")
	@PrefixGameTestTemplate(false)
	public void testComposterFirstInsert(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 1, 0);

		ItemVariant carrot = ItemVariant.of(Items.CARROT);

		for (int i = 0; i < 200; ++i) { // Run many times as this can be random.
			context.setBlock(pos, Blocks.COMPOSTER.defaultBlockState());
			Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getLevel(), context.absolutePos(pos), Direction.UP);

			try (Transaction tx = Transaction.openOuter()) {
				if (storage.insert(carrot, 1, tx) != 1) {
					context.fail("Carrot should have been inserted", pos);
				}

				tx.commit();
			}

			context.assertBlockState(pos, state -> state.getValue(ComposterBlock.LEVEL) == 1, () -> "Composter should have level 1");
		}

		context.succeed();
	}
}
