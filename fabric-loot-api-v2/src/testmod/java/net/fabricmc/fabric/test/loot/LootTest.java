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

package net.fabricmc.fabric.test.loot;

import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_loot_api_v2_testmod")
public class LootTest {

	public LootTest() {
		// Test loot table load event
		// The LootTable.Builder LootPool.Builder methods here should use
		// prebuilt entries and pools to test the injected methods.
		LootTableEvents.REPLACE.register((resourceManager, lootManager, id, original, source) -> {
			if (Blocks.BLACK_WOOL.getLootTable().equals(id)) {
				if (source != LootTableSource.VANILLA) {
					throw new AssertionError("black wool loot table should have LootTableSource.VANILLA");
				}

				// Replace black wool drops with an iron ingot
				LootPool.Builder pool = LootPool.lootPool()
						.add(LootItem.lootTableItem(Items.IRON_INGOT));

				return LootTable.lootTable().withPool(pool).build();
			}

			return null;
		});

		// Test that the event is stopped when the loot table is replaced
		LootTableEvents.REPLACE.register((resourceManager, lootManager, id, original, source) -> {
			if (Blocks.BLACK_WOOL.getLootTable().equals(id)) {
				throw new AssertionError("Event should have been stopped from replaced loot table");
			}

			return null;
		});

		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
			if (Blocks.BLACK_WOOL.getLootTable().equals(id) && source != LootTableSource.REPLACED) {
				throw new AssertionError("black wool loot table should have LootTableSource.REPLACED");
			}

			if (Blocks.WHITE_WOOL.getLootTable().equals(id)) {
				if (source != LootTableSource.VANILLA) {
					throw new AssertionError("white wool loot table should have LootTableSource.VANILLA");
				}

				// Add gold ingot with custom name to white wool drops
				LootPool.Builder pool = LootPool.lootPool()
						.add(LootItem.lootTableItem(Items.GOLD_INGOT))
						.when(ExplosionCondition.survivesExplosion())
						.apply(SetNameFunction.setName(Component.literal("Gold from White Wool")));

				tableBuilder.withPool(pool);
			}

			// We modify red wool to drop diamonds in the test mod resources.
			if (Blocks.RED_WOOL.getLootTable().equals(id) && source != LootTableSource.MOD) {
				throw new AssertionError("red wool loot table should have LootTableSource.MOD");
			}

			// Modify yellow wool to drop *either* yellow wool or emeralds by adding
			// emeralds to the same loot pool.
			if (Blocks.YELLOW_WOOL.getLootTable().equals(id)) {
				((FabricLootTableBuilder) tableBuilder).modifyPools(poolBuilder -> poolBuilder.add(LootItem.lootTableItem(Items.EMERALD)));
			}
		});
	}
}
