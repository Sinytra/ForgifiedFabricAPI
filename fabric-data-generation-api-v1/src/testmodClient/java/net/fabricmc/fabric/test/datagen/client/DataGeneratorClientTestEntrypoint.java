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

package net.fabricmc.fabric.test.datagen.client;

import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.MOD_ID;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.JsonKeySortOrderCallback;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class DataGeneratorClientTestEntrypoint implements DataGeneratorEntrypoint {
	@Override
	public void addJsonKeySortOrders(JsonKeySortOrderCallback callback) {
		callback.add("type", 100); // Force 'type' at the end
	}

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		final FabricDataGenerator.Pack pack = dataGenerator.createBuiltinResourcePack(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "example_builtin"));
		pack.addProvider(TestAtlasSourceProvider::new);
	}

	private static class TestAtlasSourceProvider extends FabricCodecDataProvider<List<SpriteSource>> {
		private TestAtlasSourceProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(dataOutput, registriesFuture, PackOutput.Target.RESOURCE_PACK, "atlases", SpriteSources.FILE_CODEC);
		}

		@Override
		protected void configure(BiConsumer<ResourceLocation, List<SpriteSource>> provider, HolderLookup.Provider lookup) {
			provider.accept(ResourceLocation.fromNamespaceAndPath(MOD_ID, "atlas_source_test"), List.of(new DirectoryLister("example", "example/")));
		}

		@Override
		public String getName() {
			return "Atlas Sources";
		}
	}
}
