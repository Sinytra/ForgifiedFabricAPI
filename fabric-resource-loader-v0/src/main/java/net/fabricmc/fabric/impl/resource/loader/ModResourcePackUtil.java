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

package net.fabricmc.fabric.impl.resource.loader;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * Internal utilities for managing resource packs.
 */
public final class ModResourcePackUtil {
	private static final Gson GSON = new Gson();

	private ModResourcePackUtil() {
	}

	/**
	 * Appends mod resource packs to the given list.
	 *
	 * @param packs   the resource pack list to append
	 * @param type    the type of resource
	 * @param subPath the resource pack sub path directory in mods, may be {@code null}
	 */
	public static void appendModResourcePacks(List<ModResourcePack> packs, ResourceType type, @Nullable String subPath) {
		for (IModInfo container : ModList.get().getMods()) {
			ModResourcePack pack = ModNioResourcePack.create(container.getModId(), Either.right(container), subPath, type, ResourcePackActivationType.ALWAYS_ENABLED);

			if (pack != null) {
				packs.add(pack);
			}
		}
	}

	public static String serializeMetadata(int packVersion, String description) {
		JsonObject pack = new JsonObject();
		pack.addProperty("pack_format", packVersion);
		pack.addProperty("description", description);
		JsonObject metadata = new JsonObject();
		metadata.add("pack", pack);
		return GSON.toJson(metadata);
	}

	public static Text getName(ModMetadata info) {
		if (info.getName() != null) {
			return Text.literal(info.getName());
		} else {
			return Text.translatable("pack.name.fabricMod", info.getId());
		}
	}
}
