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

package net.fabricmc.fabric.impl.resource.conditions;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class ResourceConditionsImpl {
	public static final Logger LOGGER = LoggerFactory.getLogger("Fabric Resource Conditions");

	// Providers

	public static ConditionJsonProvider array(ResourceLocation id, ConditionJsonProvider... values) {
		Preconditions.checkArgument(values.length > 0, "Must register at least one value.");

		return new ConditionJsonProvider() {
			@Override
			public ResourceLocation getConditionId() {
				return id;
			}

			@Override
			public void writeParameters(JsonObject object) {
				JsonArray array = new JsonArray();

				for (ConditionJsonProvider provider : values) {
					array.add(provider.toJson());
				}

				object.add("values", array);
			}
		};
	}

	public static ConditionJsonProvider mods(ResourceLocation id, String... modIds) {
		Preconditions.checkArgument(modIds.length > 0, "Must register at least one mod id.");

		return new ConditionJsonProvider() {
			@Override
			public ResourceLocation getConditionId() {
				return id;
			}

			@Override
			public void writeParameters(JsonObject object) {
				JsonArray array = new JsonArray();

				for (String modId : modIds) {
					array.add(modId);
				}

				object.add("values", array);
			}
		};
	}

	@SafeVarargs
	public static <T> ConditionJsonProvider tagsPopulated(ResourceLocation id, boolean includeRegistry, TagKey<T>... tags) {
		Preconditions.checkArgument(tags.length > 0, "Must register at least one tag.");
		final ResourceKey<? extends Registry<?>> registryRef = tags[0].registry();

		return new ConditionJsonProvider() {
			@Override
			public ResourceLocation getConditionId() {
				return id;
			}

			@Override
			public void writeParameters(JsonObject object) {
				JsonArray array = new JsonArray();

				for (TagKey<T> tag : tags) {
					array.add(tag.location().toString());
				}

				object.add("values", array);

				if (includeRegistry && registryRef != Registries.ITEM) {
					// tags[0] is guaranteed to exist.
					// Skip if this is the default (minecraft:item)
					object.addProperty("registry", registryRef.location().toString());
				}
			}
		};
	}

	public static ConditionJsonProvider featuresEnabled(ResourceLocation id, final FeatureFlag... features) {
		final Set<ResourceLocation> ids = new TreeSet<>(FeatureFlags.REGISTRY.toNames(FeatureFlags.REGISTRY.subset(features)));

		return new ConditionJsonProvider() {
			@Override
			public ResourceLocation getConditionId() {
				return id;
			}

			@Override
			public void writeParameters(JsonObject object) {
				JsonArray array = new JsonArray();

				for (ResourceLocation id : ids) {
					array.add(id.toString());
				}

				object.add("features", array);
			}
		};
	}

	public static ConditionJsonProvider registryContains(ResourceLocation id, ResourceLocation registry, ResourceLocation... entries) {
		Preconditions.checkArgument(entries.length > 0, "Must register at least one entry.");

		return new ConditionJsonProvider() {
			@Override
			public ResourceLocation getConditionId() {
				return id;
			}

			@Override
			public void writeParameters(JsonObject object) {
				JsonArray array = new JsonArray();

				for (ResourceLocation entry : entries) {
					array.add(entry.toString());
				}

				object.add("values", array);

				if (!Registries.ITEM.location().equals(registry)) {
					// Skip if this is the default (minecraft:item)
					object.addProperty("registry", registry.toString());
				}
			}
		};
	}

	// Condition implementations

	public static boolean modsLoadedMatch(JsonObject object, boolean and) {
		JsonArray array = GsonHelper.getAsJsonArray(object, "values");

		for (JsonElement element : array) {
			if (element.isJsonPrimitive()) {
				if (ModList.get().isLoaded(element.getAsString()) != and) {
					return !and;
				}
			} else {
				throw new JsonParseException("Invalid mod id entry: " + element);
			}
		}

		return and;
	}

	/**
	 * Stores the tags deserialized by {@link TagManager} before they are bound, to use them in the tags_populated conditions.
	 * The tags are set at the end of the "apply" phase in {@link TagManager}, and cleared in {@link net.minecraft.server.ReloadableServerResources#updateRegistryTags}.
	 * If the resource reload fails, the thread local is not cleared and:
	 * - the map will remain in memory until the next reload;
	 * - any call to {@link #tagsPopulatedMatch} will check the tags from the failed reload instead of failing directly.
	 * This is probably acceptable.
	 */
	public static final ThreadLocal<Map<ResourceKey<?>, Map<ResourceLocation, Collection<Holder<?>>>>> LOADED_TAGS = new ThreadLocal<>();

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void setTags(List<TagManager.LoadResult<?>> tags) {
		Map<ResourceKey<?>, Map<ResourceLocation, Collection<Holder<?>>>> tagMap = new HashMap<>();

		for (TagManager.LoadResult<?> registryTags : tags) {
			tagMap.put(registryTags.key(), (Map) registryTags.tags());
		}

		LOADED_TAGS.set(tagMap);
	}

	public static boolean tagsPopulatedMatch(JsonObject object) {
		String key = GsonHelper.getAsString(object, "registry", "minecraft:item");
		ResourceKey<? extends Registry<?>> registryRef = ResourceKey.createRegistryKey(new ResourceLocation(key));
		return tagsPopulatedMatch(object, registryRef);
	}

	public static boolean tagsPopulatedMatch(JsonObject object, ResourceKey<? extends Registry<?>> registryKey) {
		JsonArray array = GsonHelper.getAsJsonArray(object, "values");
		@Nullable
		Map<ResourceKey<?>, Map<ResourceLocation, Collection<Holder<?>>>> allTags = LOADED_TAGS.get();

		if (allTags == null) {
			LOGGER.warn("Can't retrieve deserialized tags. Failing tags_populated resource condition check.");
			return false;
		}

		Map<ResourceLocation, Collection<Holder<?>>> registryTags = allTags.get(registryKey);

		if (registryTags == null) {
			// No tag for this registry
			return array.isEmpty();
		}

		for (JsonElement element : array) {
			if (element.isJsonPrimitive()) {
				ResourceLocation id = new ResourceLocation(element.getAsString());
				Collection<Holder<?>> tags = registryTags.get(id);

				if (tags == null || tags.isEmpty()) {
					return false;
				}
			} else {
				throw new JsonParseException("Invalid tag id entry: " + element);
			}
		}

		return true;
	}

	public static final ThreadLocal<FeatureFlagSet> CURRENT_FEATURES = ThreadLocal.withInitial(() -> FeatureFlags.DEFAULT_FLAGS);

	public static boolean featuresEnabledMatch(JsonObject object) {
		List<ResourceLocation> featureIds = GsonHelper.getAsJsonArray(object, "features").asList().stream().map((element) -> new ResourceLocation(element.getAsString())).toList();
		FeatureFlagSet set = FeatureFlags.REGISTRY.fromNames(featureIds, (id) -> {
			throw new JsonParseException("Unknown feature flag: " + id);
		});

		return set.isSubsetOf(CURRENT_FEATURES.get());
	}

	public static final ThreadLocal<RegistryAccess.Frozen> CURRENT_REGISTRIES = new ThreadLocal<>();

	public static boolean registryContainsMatch(JsonObject object) {
		String key = GsonHelper.getAsString(object, "registry", "minecraft:item");
		ResourceKey<? extends Registry<?>> registryRef = ResourceKey.createRegistryKey(new ResourceLocation(key));
		return registryContainsMatch(object, registryRef);
	}

	private static <E> boolean registryContainsMatch(JsonObject object, ResourceKey<? extends Registry<? extends E>> registryRef) {
		JsonArray array = GsonHelper.getAsJsonArray(object, "values");
		RegistryAccess.Frozen registries = CURRENT_REGISTRIES.get();

		if (registries == null) {
			LOGGER.warn("Can't retrieve current registries. Failing registry_contains resource condition check.");
			return false;
		}

		Optional<Registry<E>> registry = registries.registry(registryRef);

		if (registry.isEmpty()) {
			// No such registry
			return array.isEmpty();
		}

		for (JsonElement element : array) {
			if (element.isJsonPrimitive()) {
				ResourceLocation id = new ResourceLocation(element.getAsString());

				if (!registry.get().containsKey(id)) {
					return false;
				}
			} else {
				throw new JsonParseException("Invalid registry entry id: " + element);
			}
		}

		return true;
	}
}
