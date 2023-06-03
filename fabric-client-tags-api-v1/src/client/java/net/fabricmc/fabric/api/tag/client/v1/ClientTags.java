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

package net.fabricmc.fabric.api.tag.client.v1;

import net.fabricmc.fabric.impl.tag.client.ClientTagsLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows the use of tags by directly loading them from the installed mods.
 *
 * <p>Tags are loaded by the server, either the internal server in singleplayer or the connected server and
 * synced to the client. This can be a pain point for interoperability, as a tag that does not exist on the server
 * because it is part of a mod only present on the client will no longer be available to the client that may wish to
 * query it.
 *
 * <p>Client Tags resolve that issue by lazily reading the tag json files within the mods on the side of the caller,
 * directly, allowing for mods to query tags such as {@link net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags}
 * even when connected to a vanilla server.
 */
public final class ClientTags {
	private static final Map<TagKey<?>, Set<ResourceLocation>> LOCAL_TAG_CACHE = new ConcurrentHashMap<>();

	private ClientTags() {
	}

	/**
	 * Loads a tag into the cache, recursively loading any contained tags along with it.
	 *
	 * @param tagKey the {@code TagKey} to load
	 * @return a set of {@code Identifier}s this tag contains
	 */
	public static Set<ResourceLocation> getOrCreateLocalTag(TagKey<?> tagKey) {
		Set<ResourceLocation> ids = LOCAL_TAG_CACHE.get(tagKey);

		if (ids == null) {
			ids = ClientTagsLoader.loadTag(tagKey);
			LOCAL_TAG_CACHE.put(tagKey, ids);
		}

		return ids;
	}

	/**
	 * Checks if an entry is in a tag.
	 *
	 * <p>If the synced tag does exist, it is queried. If it does not exist,
	 * the tag populated from the available mods is checked.
	 *
	 * @param tagKey the {@code TagKey} to being checked
	 * @param entry  the entry to check
	 * @return if the entry is in the given tag
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, T entry) {
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(entry);

		Optional<? extends Registry<?>> maybeRegistry = getRegistry(tagKey);

		if (maybeRegistry.isEmpty()) {
			return false;
		}

		if (!tagKey.isFor(maybeRegistry.get().key())) {
			return false;
		}

		Registry<T> registry = (Registry<T>) maybeRegistry.get();

		Optional<ResourceKey<T>> maybeKey = registry.getResourceKey(entry);

		// Check synced tag
		if (registry.getTag(tagKey).isPresent()) {
			return maybeKey.filter(registryKey -> registry.getHolderOrThrow(registryKey).is(tagKey))
					.isPresent();
		}

		// Check local tags
		Set<ResourceLocation> ids = getOrCreateLocalTag(tagKey);
		return maybeKey.filter(registryKey -> ids.contains(registryKey.location())).isPresent();
	}

	/**
	 * Checks if an entry is in a tag, for use with entries from a dynamic registry,
	 * such as {@link Biome}s.
	 *
	 * <p>If the synced tag does exist, it is queried. If it does not exist,
	 * the tag populated from the available mods is checked.
	 *
	 * @param tagKey        the {@code TagKey} to be checked
	 * @param registryEntry the entry to check
	 * @return if the entry is in the given tag
	 */
	public static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, Holder<T> registryEntry) {
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(registryEntry);

		// Check if the tag exists in the dynamic registry first
		Optional<? extends Registry<T>> maybeRegistry = getRegistry(tagKey);

		if (maybeRegistry.isPresent()) {
			if (maybeRegistry.get().getTag(tagKey).isPresent()) {
				return registryEntry.is(tagKey);
			}
		}

		if (registryEntry.unwrapKey().isPresent()) {
			return isInLocal(tagKey, registryEntry.unwrapKey().get());
		}

		return false;
	}

	/**
	 * Checks if an entry is in a tag provided by the available mods.
	 *
	 * @param tagKey      the {@code TagKey} to being checked
	 * @param registryKey the entry to check
	 * @return if the entry is in the given tag
	 */
	public static <T> boolean isInLocal(TagKey<T> tagKey, ResourceKey<T> registryKey) {
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(registryKey);

		if (tagKey.registry().location().equals(registryKey.registry())) {
			// Check local tags
			Set<ResourceLocation> ids = getOrCreateLocalTag(tagKey);
			return ids.contains(registryKey.location());
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private static <T> Optional<? extends Registry<T>> getRegistry(TagKey<T> tagKey) {
		Objects.requireNonNull(tagKey);

		// Check if the tag represents a dynamic registry
		if (Minecraft.getInstance() != null) {
			if (Minecraft.getInstance().level != null) {
				if (Minecraft.getInstance().level.registryAccess() != null) {
					Optional<? extends Registry<T>> maybeRegistry = Minecraft.getInstance().level
							.registryAccess().registry(tagKey.registry());
					if (maybeRegistry.isPresent()) return maybeRegistry;
				}
			}
		}

		return (Optional<? extends Registry<T>>) BuiltInRegistries.REGISTRY.getOptional(tagKey.registry().location());
	}
}
