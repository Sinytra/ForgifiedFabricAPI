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

package net.fabricmc.fabric.impl.recipe.sync.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.sinytra.fabric.recipe_api.generated.GeneratedEntryPoint;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.fabricmc.fabric.impl.recipe.sync.SynchronizedRecipesImpl;

@Mod(GeneratedEntryPoint.MOD_ID)
public class RecipeSyncImplClient {

	public RecipeSyncImplClient(IEventBus bus) {
		NeoForge.EVENT_BUS.addListener(RecipesReceivedEvent.class, RecipeSyncImplClient::onNeoRecipesReceives);
	}

	private static void onNeoRecipesReceives(RecipesReceivedEvent event) {
		SynchronizedRecipes recipes;
		Collection<RecipeHolder<?>> received = event.getRecipeMap().values();

		if (!received.isEmpty()) {
			var collectedRecipes = new ArrayList<>(received);
			// Sort values by id to match ordering with server ones.
			collectedRecipes.sort(Comparator.comparing(entry -> entry.id().identifier()));
			recipes = SynchronizedRecipesImpl.of(collectedRecipes);
		} else {
			recipes = SynchronizedRecipesImpl.EMPTY;
		}

		((SynchronizedClientRecipesSetter) Minecraft.getInstance().player.connection.recipes()).fabric_setSynchronizedClientRecipes(recipes);
		ClientRecipeSynchronizedEvent.EVENT.invoker().onRecipesSynchronized(Minecraft.getInstance(), recipes);
	}
}
