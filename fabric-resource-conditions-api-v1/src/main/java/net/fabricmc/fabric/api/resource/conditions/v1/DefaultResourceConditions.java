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

package net.fabricmc.fabric.api.resource.conditions.v1;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Contains {@link ConditionJsonProvider}s for resource conditions provided by fabric itself.
 */
public final class DefaultResourceConditions {
	private static final ResourceLocation NOT = new ResourceLocation("fabric:not");
	private static final ResourceLocation AND = new ResourceLocation("fabric:and");
	private static final ResourceLocation OR = new ResourceLocation("fabric:or");
	private static final ResourceLocation ALL_MODS_LOADED = new ResourceLocation("fabric:all_mods_loaded");
	private static final ResourceLocation ANY_MOD_LOADED = new ResourceLocation("fabric:any_mod_loaded");
	private static final ResourceLocation BLOCK_TAGS_POPULATED = new ResourceLocation("fabric:block_tags_populated");
	private static final ResourceLocation FLUID_TAGS_POPULATED = new ResourceLocation("fabric:fluid_tags_populated");
	private static final ResourceLocation ITEM_TAGS_POPULATED = new ResourceLocation("fabric:item_tags_populated");
	private static final ResourceLocation TAGS_POPULATED = new ResourceLocation("fabric:tags_populated");
	private static final ResourceLocation FEATURES_ENABLED = new ResourceLocation("fabric:features_enabled");
	private static final ResourceLocation REGISTRY_CONTAINS = new ResourceLocation("fabric:registry_contains");

	/**
	 * Creates a NOT condition that returns true if its child condition is false, and false if its child is true.
	 *
	 * @apiNote This condition's ID is {@code fabric:not}, and takes one property, {@code value},
	 * which is a condition.
	 */
	public static ConditionJsonProvider not(ConditionJsonProvider value) {
		return new ConditionJsonProvider() {
			@Override
			public void writeParameters(JsonObject object) {
				object.add("value", value.toJson());
			}

			@Override
			public ResourceLocation getConditionId() {
				return NOT;
			}
		};
	}

	/**
	 * Creates a condition that returns true if all of its child conditions are true.
	 *
	 * @apiNote This condition's ID is {@code fabric:and}, and takes one property, {@code values},
	 * which is an array of conditions.
	 */
	public static ConditionJsonProvider and(ConditionJsonProvider... values) {
		return ResourceConditionsImpl.array(AND, values);
	}

	/**
	 * Creates a condition that returns true if any of its child conditions are true.
	 *
	 * @apiNote This condition's ID is {@code fabric:or}, and takes one property, {@code values},
	 * which is an array of conditions.
	 */
	public static ConditionJsonProvider or(ConditionJsonProvider... values) {
		return ResourceConditionsImpl.array(OR, values);
	}

	/**
	 * Creates a condition that returns true if all the passed mod ids correspond to a loaded mod.
	 *
	 * @apiNote This condition's ID is {@code fabric:all_mods_loaded}, and takes one property,
	 * {@code values}, which is an array of string mod IDs.
	 */
	public static ConditionJsonProvider allModsLoaded(String... modIds) {
		return ResourceConditionsImpl.mods(ALL_MODS_LOADED, modIds);
	}

	/**
	 * Creates a condition that returns true if at least one of the passed mod ids corresponds to a loaded mod.
	 *
	 * @apiNote This condition's ID is {@code fabric:any_mod_loaded}, and takes one property,
	 * {@code values}, which is an array of string mod IDs.
	 */
	public static ConditionJsonProvider anyModLoaded(String... modIds) {
		return ResourceConditionsImpl.mods(ANY_MOD_LOADED, modIds);
	}

	/**
	 * Create a condition that returns true if each of the passed block tags exists and has at least one element.
	 * @deprecated Use {@link #tagsPopulated} instead.
	 */
	@SafeVarargs
	@Deprecated
	public static ConditionJsonProvider blockTagsPopulated(TagKey<Block>... tags) {
		return ResourceConditionsImpl.tagsPopulated(BLOCK_TAGS_POPULATED, false, tags);
	}

	/**
	 * Create a condition that returns true if each of the passed fluid tags exists and has at least one element.
	 * @deprecated Use {@link #tagsPopulated} instead.
	 */
	@SafeVarargs
	@Deprecated
	public static ConditionJsonProvider fluidTagsPopulated(TagKey<Fluid>... tags) {
		return ResourceConditionsImpl.tagsPopulated(FLUID_TAGS_POPULATED, false, tags);
	}

	/**
	 * Create a condition that returns true if each of the passed item tags exists and has at least one element.
	 * @deprecated Use {@link #tagsPopulated} instead.
	 */
	@SafeVarargs
	@Deprecated
	public static ConditionJsonProvider itemTagsPopulated(TagKey<Item>... tags) {
		return ResourceConditionsImpl.tagsPopulated(ITEM_TAGS_POPULATED, false, tags);
	}

	/**
	 * Creates a condition that returns true if each of the passed tags exists and has at least one element.
	 * This works for any registries, and the registry ID of the tags is serialized to JSON as well as the tags.
	 *
	 * @apiNote This condition's ID is {@code fabric:tags_populated}, and takes up to two properties:
	 * {@code values}, which is an array of string tag IDs, and {@code registry}, which is the ID of
	 * the registry of the tags. If {@code registry} is not provided, it defaults to {@code minecraft:item}.
	 */
	@SafeVarargs
	public static <T> ConditionJsonProvider tagsPopulated(TagKey<T>... tags) {
		return ResourceConditionsImpl.tagsPopulated(TAGS_POPULATED, true, tags);
	}

	/**
	 * Creates a condition that returns true if all the passed features are enabled.
	 * @param features the features to check for
	 *
	 * @apiNote This condition's ID is {@code fabric:features_enabled}, and takes one property:
	 * {@code features}, which is the array of the IDs of the feature flag to check.
	 */
	public static ConditionJsonProvider featuresEnabled(FeatureFlag... features) {
		return ResourceConditionsImpl.featuresEnabled(FEATURES_ENABLED, features);
	}

	/**
	 * Creates a condition that returns true if all the passed items are registered (in {@link BuiltInRegistries#ITEM}).
	 *
	 * @see #registryContains(ResourceKey, ResourceLocation...)
	 */
	public static ConditionJsonProvider itemsRegistered(ItemLike... items) {
		return registryContains(BuiltInRegistries.ITEM, transform(items, ItemLike::asItem));
	}

	/**
	 * Creates a condition that returns true if the registry contains all the passed entries,
	 * i.e. if all the passed registry entries are loaded.
	 *
	 * @see #registryContains(ResourceKey, ResourceLocation...)
	 */
	@SafeVarargs
	public static <T> ConditionJsonProvider registryContains(Registry<T> registry, T... entries) {
		return registryContains(transform(entries, e -> {
			return registry.getResourceKey(e).orElseThrow(() -> new IllegalArgumentException("Entry is not registered"));
		}));
	}

	/**
	 * Creates a condition that returns true if all the passed registry entries are loaded.
	 *
	 * @see #registryContains(ResourceKey, ResourceLocation...)
	 */
	@SafeVarargs
	public static <T> ConditionJsonProvider registryContains(ResourceKey<T>... entries) {
		Preconditions.checkArgument(entries.length > 0, "Must register at least one entry.");

		return registryContains(
				ResourceKey.createRegistryKey(entries[0].registry()),
				transform(entries, ResourceKey::location));
	}

	/**
	 * Creates a condition that returns true if all the passed registry entries are loaded.
	 * Dynamic registries are supported for server resources.
	 *
	 * @apiNote This condition's ID is {@code fabric:registry_contains}, and takes up to two properties:
	 * {@code values}, which is an array of string registry entry IDs, and {@code registry}, which is the ID of
	 * the registry of the entries. If {@code registry} is not provided, it defaults to {@code minecraft:item}.
	 */
	public static <T> ConditionJsonProvider registryContains(ResourceKey<Registry<T>> registry, ResourceLocation... entries) {
		return ResourceConditionsImpl.registryContains(REGISTRY_CONTAINS, registry.location(), entries);
	}

	static void init() {
		// init static
	}

	static {
		ResourceConditions.register(NOT, object -> {
			JsonObject condition = GsonHelper.getAsJsonObject(object, "value");
			return !ResourceConditions.conditionMatches(condition);
		});
		ResourceConditions.register(AND, object -> {
			JsonArray array = GsonHelper.getAsJsonArray(object, "values");
			return ResourceConditions.conditionsMatch(array, true);
		});
		ResourceConditions.register(OR, object -> {
			JsonArray array = GsonHelper.getAsJsonArray(object, "values");
			return ResourceConditions.conditionsMatch(array, false);
		});
		ResourceConditions.register(ALL_MODS_LOADED, object -> ResourceConditionsImpl.modsLoadedMatch(object, true));
		ResourceConditions.register(ANY_MOD_LOADED, object -> ResourceConditionsImpl.modsLoadedMatch(object, false));
		ResourceConditions.register(BLOCK_TAGS_POPULATED, object -> ResourceConditionsImpl.tagsPopulatedMatch(object, Registries.BLOCK));
		ResourceConditions.register(FLUID_TAGS_POPULATED, object -> ResourceConditionsImpl.tagsPopulatedMatch(object, Registries.FLUID));
		ResourceConditions.register(ITEM_TAGS_POPULATED, object -> ResourceConditionsImpl.tagsPopulatedMatch(object, Registries.ITEM));
		ResourceConditions.register(TAGS_POPULATED, ResourceConditionsImpl::tagsPopulatedMatch);
		ResourceConditions.register(FEATURES_ENABLED, ResourceConditionsImpl::featuresEnabledMatch);
		ResourceConditions.register(REGISTRY_CONTAINS, ResourceConditionsImpl::registryContainsMatch);
	}

	// Slightly gross - the empty outputType vararg is used to capture the correct type for B[]
	@SafeVarargs
	private static <A, B> B[] transform(A[] input, Function<A, B> mapper, B... outputType) {
		B[] output = Arrays.copyOf(outputType, input.length);

		for (int i = 0; i < input.length; i++) {
			output[i] = mapper.apply(input[i]);
		}

		return output;
	}

	private DefaultResourceConditions() {
	}
}
