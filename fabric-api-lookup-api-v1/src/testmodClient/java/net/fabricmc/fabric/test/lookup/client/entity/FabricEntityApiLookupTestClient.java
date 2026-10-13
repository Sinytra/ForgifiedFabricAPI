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

package net.fabricmc.fabric.test.lookup.client.entity;

import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.client.core.registries.ClientRegistries;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.pig.Pig;

import net.fabricmc.fabric.test.lookup.entity.FabricEntityApiLookupTest;

public class FabricEntityApiLookupTestClient {
	public static void onInitializeClient() {
		ResourceKey<EntityRendererProvider<?>> key = ResourceKey.create(
				ClientRegistries.ENTITY_RENDERER,
				BuiltInRegistries.ENTITY_TYPE.getKey(FabricEntityApiLookupTest.INSPECTABLE_PIG)
		);
		EntityRendererProvider<Pig> provider = InspectablePigRenderer::new;
		Registry.register(BuiltInClientRegistries.ENTITY_RENDERER, key, provider);
	}
}
