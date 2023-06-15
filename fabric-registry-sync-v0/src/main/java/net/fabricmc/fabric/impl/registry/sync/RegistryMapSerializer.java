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

package net.fabricmc.fabric.impl.registry.sync;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegistryMapSerializer {
	public static final int VERSION = 1;

	public static Map<ResourceLocation, Object2IntMap<ResourceLocation>> fromNbt(CompoundTag nbt) {
		CompoundTag mainNbt = nbt.getCompound("registries");
		Map<ResourceLocation, Object2IntMap<ResourceLocation>> map = new LinkedHashMap<>();

		for (String registryId : mainNbt.getAllKeys()) {
			Object2IntMap<ResourceLocation> idMap = new Object2IntLinkedOpenHashMap<>();
			CompoundTag idNbt = mainNbt.getCompound(registryId);

			for (String id : idNbt.getAllKeys()) {
				idMap.put(new ResourceLocation(id), idNbt.getInt(id));
			}

			map.put(new ResourceLocation(registryId), idMap);
		}

		return map;
	}

	public static CompoundTag toNbt(Map<ResourceLocation, Object2IntMap<ResourceLocation>> map) {
		CompoundTag mainNbt = new CompoundTag();

		map.forEach((registryId, idMap) -> {
			CompoundTag registryNbt = new CompoundTag();

			for (Object2IntMap.Entry<ResourceLocation> idPair : idMap.object2IntEntrySet()) {
				registryNbt.putInt(idPair.getKey().toString(), idPair.getIntValue());
			}

			mainNbt.put(registryId.toString(), registryNbt);
		});

		CompoundTag nbt = new CompoundTag();
		nbt.putInt("version", VERSION);
		nbt.put("registries", mainNbt);
		return nbt;
	}
}
