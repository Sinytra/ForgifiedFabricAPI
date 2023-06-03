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

package net.fabricmc.fabric.impl.tag.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientTagsLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-tags-api-v1");

	/**
	 * Load a given tag from the available mods into a set of {@code Identifier}s.
	 * Parsing based on {@link net.minecraft.tags.TagLoader#load(ResourceManager)}
	 */
	public static Set<ResourceLocation> loadTag(TagKey<?> tagKey) {
		var tags = new HashSet<TagEntry>();
		HashSet<Path> tagFiles = getTagFiles(tagKey.registry(), tagKey.location());

		for (Path tagPath : tagFiles) {
			try (BufferedReader tagReader = Files.newBufferedReader(tagPath)) {
				JsonElement jsonElement = JsonParser.parseReader(tagReader);
				TagFile maybeTagFile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonElement))
					.result().orElse(null);

				if (maybeTagFile != null) {
					if (maybeTagFile.replace()) {
						tags.clear();
					}

					tags.addAll(maybeTagFile.entries());
				}
			} catch (IOException e) {
				LOGGER.error("Error loading tag: " + tagKey, e);
			}
		}

		HashSet<ResourceLocation> ids = new HashSet<>();

		for (TagEntry tagEntry : tags) {
			tagEntry.build(new TagEntry.Lookup<>() {
				@Nullable
				@Override
				public ResourceLocation element(ResourceLocation id) {
					return id;
				}

				@Nullable
				@Override
				public Collection<ResourceLocation> tag(ResourceLocation id) {
					TagKey<?> tag = TagKey.create(tagKey.registry(), id);
					return ClientTags.getOrCreateLocalTag(tag);
				}
			}, ids::add);
		}

		return Collections.unmodifiableSet(ids);
	}

	/**
	 * @param registryKey the RegistryKey of the TagKey
	 * @param identifier  the Identifier of the tag
	 * @return the paths to all tag json files within the available mods
	 */
	private static HashSet<Path> getTagFiles(ResourceKey<? extends Registry<?>> registryKey, ResourceLocation identifier) {
		return getTagFiles(TagManager.getTagDir(registryKey), identifier);
	}

	/**
	 * @return the paths to all tag json files within the available mods
	 */
	private static HashSet<Path> getTagFiles(String tagType, ResourceLocation identifier) {
		String tagFile = "data/%s/%s/%s.json".formatted(identifier.getNamespace(), tagType, identifier.getPath());
		return getResourcePaths(tagFile);
	}

	/**
	 * @return all paths from the available mods that match the given internal path
	 */
	private static HashSet<Path> getResourcePaths(String path) {
		HashSet<Path> out = new HashSet<>();

		for (IModFileInfo modFile : ModList.get().getModFiles()) {
			Path modPath = modFile.getFile().findResource(path);
			if (Files.exists(modPath)) {
				out.add(modPath);
			}
		}

		return out;
	}
}
