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

package net.fabricmc.fabric.impl.loot;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.fabricmc.fabric.mixin.loot.ResourcePackLoaderAccessor;

public final class LootUtil {
	public static LootTableSource determineSource(Identifier lootTableId, ResourceManager resourceManager) {
		Identifier resourceId = new Identifier(lootTableId.getNamespace(), "loot_tables/%s.json".formatted(lootTableId.getPath()));

		Resource resource = resourceManager.getResource(resourceId).orElse(null);

		if (resource != null) {
			ResourcePack resources = resource.getPack();

			if (resources.isAlwaysStable()) {
				return LootTableSource.VANILLA;
			}
			else if (ResourcePackLoaderAccessor.getModResourcePacks().containsValue(resources)) {
				return LootTableSource.MOD;
			}
		}

		// If not builtin or mod, assume external data pack.
		// It might also be a virtual loot table injected via mixin instead of being loaded
		// from a resource, but we can't determine that here.
		return LootTableSource.DATA_PACK;
	}
}
