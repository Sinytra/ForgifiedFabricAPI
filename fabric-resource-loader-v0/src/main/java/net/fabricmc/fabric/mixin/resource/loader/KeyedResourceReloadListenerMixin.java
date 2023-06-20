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

package net.fabricmc.fabric.mixin.resource.loader;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.LootTables;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

@Mixin({RecipeManager.class, ServerAdvancementManager.class, ServerFunctionLibrary.class, LootTables.class, TagManager.class})
public abstract class KeyedResourceReloadListenerMixin implements IdentifiableResourceReloadListener {
	private ResourceLocation fabric$id;
	private Collection<ResourceLocation> fabric$dependencies;

	@Override
	@SuppressWarnings("ConstantConditions")
	public ResourceLocation getFabricId() {
		if (this.fabric$id == null) {
			Object self = this;

			if (self instanceof RecipeManager) {
				this.fabric$id = ResourceReloadListenerKeys.RECIPES;
			} else if (self instanceof ServerAdvancementManager) {
				this.fabric$id = ResourceReloadListenerKeys.ADVANCEMENTS;
			} else if (self instanceof ServerFunctionLibrary) {
				this.fabric$id = ResourceReloadListenerKeys.FUNCTIONS;
			} else if (self instanceof LootTables) {
				this.fabric$id = ResourceReloadListenerKeys.LOOT_TABLES;
			} else if (self instanceof TagManager) {
				this.fabric$id = ResourceReloadListenerKeys.TAGS;
			} else {
				this.fabric$id = new ResourceLocation("minecraft", "private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.fabric$id;
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public Collection<ResourceLocation> getFabricDependencies() {
		if (this.fabric$dependencies == null) {
			Object self = this;

			if (self instanceof TagManager) {
				this.fabric$dependencies = Collections.emptyList();
			} else {
				this.fabric$dependencies = Collections.singletonList(ResourceReloadListenerKeys.TAGS);
			}
		}

		return this.fabric$dependencies;
	}
}
