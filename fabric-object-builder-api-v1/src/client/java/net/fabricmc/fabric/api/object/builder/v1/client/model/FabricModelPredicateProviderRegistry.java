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

package net.fabricmc.fabric.api.object.builder.v1.client.model;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Allows registering model predicate providers for item models.
 *
 * <p>A registered model predicate providers for an item can be retrieved through
 * {@link net.minecraft.client.renderer.item.ItemProperties#getProperty(Item, ResourceLocation)}.</p>
 *
 * @see net.minecraft.client.renderer.item.ItemProperties
 * @deprecated Replaced by access wideners for {@link net.minecraft.client.renderer.item.ItemProperties}
 * registration methods in Fabric Transitive Access Wideners (v1).
 */
@Deprecated
public final class FabricModelPredicateProviderRegistry {
	/**
	 * Registers a model predicate provider that is applicable for any item.
	 *
	 * @param id       the identifier of the provider
	 * @param provider the provider
	 */
	public static void register(ResourceLocation id, ClampedItemPropertyFunction provider) {
		ItemProperties.registerGeneric(id, provider);
	}

	/**
	 * Registers a model predicate provider to a specific item.
	 *
	 * @param item     the item the provider is associated to
	 * @param id       the identifier of the provider
	 * @param provider the provider
	 */
	public static void register(Item item, ResourceLocation id, ClampedItemPropertyFunction provider) {
		ItemProperties.register(item, id, provider);
	}
}
