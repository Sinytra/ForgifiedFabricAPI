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

package net.fabricmc.fabric.impl.recipe.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.RecipeContentPayload;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.mixin.recipe.sync.RecipeManagerAccessor;

public class RecipeSyncImpl implements ModInitializer {
	private static final Set<RecipeSerializer<?>> SYNCED_SERIALIZERS = new ReferenceOpenHashSet<>();

	public static final Identifier RECIPE_SYNC_EVENT_PHASE = Identifier.fromNamespaceAndPath("fabric", "recipe_sync");

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(Event.DEFAULT_PHASE, RECIPE_SYNC_EVENT_PHASE);
	}

	public static void onRecipeSyncRequest(ServerboundSupportedRecipeSerializersPayload payload, IPayloadContext context) {
		var set = new ReferenceOpenHashSet<RecipeSerializer<?>>();

		for (Identifier identifier : payload.synchronizedSerializers()) {
			BuiltInRegistries.RECIPE_SERIALIZER.getOptional(identifier).ifPresent(set::add);
		}

		((SyncedSerializerAwareConnection) context.listener().getConnection())
				.fabric_setSyncedRecipeSerializers(set);
	}

	public static RecipeContentPayload appendSyncedRecipes(RecipeContentPayload payload, ServerPlayer player) {
		List<RecipeHolder<?>> combined = new ArrayList<>(payload.recipes());
		Collection<ResourceKey<Recipe<?>>> keys = combined.stream()
			.map(RecipeHolder::id)
			.collect(Collectors.toUnmodifiableSet());

		List<RecipeHolder<?>> recipes = getRecipesToSend(player);
		for (RecipeHolder<?> recipe : recipes) {
			if (!keys.contains(recipe.id())) {
				combined.add(recipe);
			}
		}

		return new RecipeContentPayload(payload.recipeTypes(), combined);
	}

	private static List<RecipeHolder<?>> getRecipesToSend(ServerPlayer player) {
		Set<RecipeSerializer<?>> serializers = ((SyncedSerializerAwareConnection) player.connection.getConnection()).fabric_getSyncedRecipeSerializers();

		SyncedSerializerAwarePreparedRecipe accessor = (SyncedSerializerAwarePreparedRecipe) ((RecipeManagerAccessor) player.level().recipeAccess()).getRecipes();

		List<RecipeHolder<?>> list = new ArrayList<>();

		for (RecipeSerializer<?> serializer : serializers) {
			List<RecipeHolder<?>> recipes = accessor.fabric_getRecipesBySyncedSerializer(serializer);

			if (recipes != null && !recipes.isEmpty()) {
				list.addAll(recipes);
			}
		}

		return list;
	}

	public static void addSynchronizedSerializer(RecipeSerializer<?> serializer) {
		SYNCED_SERIALIZERS.add(serializer);
	}

	public static boolean isSynced(RecipeSerializer<?> serializer) {
		return SYNCED_SERIALIZERS.contains(serializer);
	}

	public static Set<RecipeSerializer<?>> getSyncedSerializers() {
		return Collections.unmodifiableSet(SYNCED_SERIALIZERS);
	}
}
