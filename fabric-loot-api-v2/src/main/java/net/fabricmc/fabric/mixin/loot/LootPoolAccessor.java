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

package net.fabricmc.fabric.mixin.loot;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accesses loot pool fields for {@link net.fabricmc.fabric.api.loot.v2.FabricLootPoolBuilder#copyOf(LootPool)}.
 * These are normally available in the transitive access widener module.
 */
@Mixin(LootPool.class)
public interface LootPoolAccessor {
	@Accessor("entries")
	LootPoolEntryContainer[] fabric_getEntries();

	@Accessor("conditions")
	LootItemCondition[] fabric_getConditions();

	@Accessor("functions")
	LootItemFunction[] fabric_getFunctions();
}
