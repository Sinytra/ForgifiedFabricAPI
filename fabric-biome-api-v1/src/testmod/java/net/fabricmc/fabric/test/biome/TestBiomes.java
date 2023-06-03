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

package net.fabricmc.fabric.test.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.biome.NetherBiomes;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class TestBiomes {
	public static final ResourceKey<Biome> EXAMPLE_BIOME = ResourceKey.create(Registries.BIOME, new ResourceLocation(FabricBiomeTest.MOD_ID, "example_biome"));
	public static final ResourceKey<Biome> TEST_CRIMSON_FOREST = ResourceKey.create(Registries.BIOME, new ResourceLocation(FabricBiomeTest.MOD_ID, "test_crimson_forest"));
	public static final ResourceKey<Biome> CUSTOM_PLAINS = ResourceKey.create(Registries.BIOME, new ResourceLocation(FabricBiomeTest.MOD_ID, "custom_plains"));
	public static final ResourceKey<Biome> TEST_END_HIGHLANDS = ResourceKey.create(Registries.BIOME, new ResourceLocation(FabricBiomeTest.MOD_ID, "test_end_highlands"));
	public static final ResourceKey<Biome> TEST_END_MIDLANDS = ResourceKey.create(Registries.BIOME, new ResourceLocation(FabricBiomeTest.MOD_ID, "test_end_midlands"));
	public static final ResourceKey<Biome> TEST_END_BARRRENS = ResourceKey.create(Registries.BIOME, new ResourceLocation(FabricBiomeTest.MOD_ID, "test_end_barrens"));

	private TestBiomes() {
	}

	public static void bootstrap(BootstapContext<Biome> biomeRegisterable) {
		HolderGetter<PlacedFeature> placedFeatures = biomeRegisterable.lookup(Registries.PLACED_FEATURE);
		HolderGetter<ConfiguredWorldCarver<?>> configuredCarvers = biomeRegisterable.lookup(Registries.CONFIGURED_CARVER);

		biomeRegisterable.register(EXAMPLE_BIOME, createExample());
		biomeRegisterable.register(TEST_CRIMSON_FOREST, NetherBiomes.crimsonForest(placedFeatures, configuredCarvers));
		biomeRegisterable.register(CUSTOM_PLAINS, OverworldBiomes.plains(placedFeatures, configuredCarvers, false, false, false));
		biomeRegisterable.register(TEST_END_HIGHLANDS, createEndHighlands(placedFeatures));
		biomeRegisterable.register(TEST_END_MIDLANDS, createEndMidlands());
		biomeRegisterable.register(TEST_END_BARRRENS, createEndBarrens());
	}

	private static Biome createExample() {
		return new Biome.BiomeBuilder()
				.temperature(0.8f)
				.downfall(0.4f)
				.hasPrecipitation(false)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.skyColor(7907327)
						.fogColor(12638463)
						.waterColor(4159204)
						.waterFogColor(329011)
						.build()
				)
				.mobSpawnSettings(
					new MobSpawnSettings.Builder().build()
				)
				.generationSettings(
					new BiomeGenerationSettings.PlainBuilder().build()
				)
				.build();
	}

	private static Biome createEndHighlands(HolderGetter<PlacedFeature> placedFeatures) {
		BiomeGenerationSettings.PlainBuilder builder = new BiomeGenerationSettings.PlainBuilder()
				.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, placedFeatures.getOrThrow(EndPlacements.END_GATEWAY_RETURN));
		return composeEndSpawnSettings(builder);
	}

	private static Biome createEndMidlands() {
		BiomeGenerationSettings.PlainBuilder builder = new BiomeGenerationSettings.PlainBuilder();
		return composeEndSpawnSettings(builder);
	}

	private static Biome createEndBarrens() {
		BiomeGenerationSettings.PlainBuilder builder = new BiomeGenerationSettings.PlainBuilder();
		return composeEndSpawnSettings(builder);
	}

	private static Biome composeEndSpawnSettings(BiomeGenerationSettings.PlainBuilder builder) {
		MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.plainsSpawns(builder2);
		return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.5F).downfall(0.5F)
			.specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(10518688).skyColor(0).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
			.mobSpawnSettings(builder2.build())
			.generationSettings(builder.build()).build();
	}
}
