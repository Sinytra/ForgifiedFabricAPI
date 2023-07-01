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

package net.fabricmc.fabric.impl.biome.modification;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeSpecialEffectsBuilder;
import net.minecraftforge.common.world.ClimateSettingsBuilder;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeParticleConfig;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.mixin.biome.SpawnSettingsBuilderAccessor;

public class BiomeModificationContextImpl implements BiomeModificationContext {
	private final DynamicRegistryManager registries;
	private final ModifiableBiomeInfo.BiomeInfo.Builder builder;
	private final WeatherContext weather;
	private final EffectsContext effects;
	private final GenerationSettingsContextImpl generationSettings;
	private final SpawnSettingsContextImpl spawnSettings;

	public BiomeModificationContextImpl(DynamicRegistryManager registries, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		this.registries = registries;
		this.builder = builder;
		this.weather = new WeatherContextImpl();
		this.effects = new EffectsContextImpl();
		this.generationSettings = new GenerationSettingsContextImpl();
		this.spawnSettings = new SpawnSettingsContextImpl();
	}

	@Override
	public WeatherContext getWeather() {
		return weather;
	}

	@Override
	public EffectsContext getEffects() {
		return effects;
	}

	@Override
	public GenerationSettingsContext getGenerationSettings() {
		return generationSettings;
	}

	@Override
	public SpawnSettingsContext getSpawnSettings() {
		return spawnSettings;
	}

	private class WeatherContextImpl implements WeatherContext {
		ClimateSettingsBuilder climateSettings = builder.getClimateSettings();
		
		@Override
		public void setPrecipitation(boolean hasPrecipitation) {
			climateSettings.setHasPrecipitation(hasPrecipitation);
		}

		@Override
		public void setTemperature(float temperature) {
			climateSettings.setTemperature(temperature);
		}

		@Override
		public void setTemperatureModifier(Biome.TemperatureModifier temperatureModifier) {
			climateSettings.setTemperatureModifier(temperatureModifier);
		}

		@Override
		public void setDownfall(float downfall) {
			climateSettings.setDownfall(downfall);
		}
	}

	private class EffectsContextImpl implements EffectsContext {
		private final BiomeSpecialEffectsBuilder effects = builder.getSpecialEffects();

		@Override
		public void setFogColor(int color) {
			effects.fogColor(color);
		}

		@Override
		public void setWaterColor(int color) {
			effects.waterColor(color);
		}

		@Override
		public void setWaterFogColor(int color) {
			effects.waterFogColor(color);
		}

		@Override
		public void setSkyColor(int color) {
			effects.skyColor(color);
		}

		@Override
		public void setFoliageColor(Optional<Integer> color) {
			color.ifPresent(effects::foliageColor);
		}

		@Override
		public void setGrassColor(Optional<Integer> color) {
			color.ifPresent(effects::grassColor);
		}

		@Override
		public void setGrassColorModifier(@NotNull BiomeEffects.GrassColorModifier colorModifier) {
			effects.grassColorModifier(colorModifier);
		}

		@Override
		public void setParticleConfig(Optional<BiomeParticleConfig> particleConfig) {
			particleConfig.ifPresent(effects::particleConfig);
		}

		@Override
		public void setAmbientSound(Optional<RegistryEntry<SoundEvent>> sound) {
			sound.ifPresent(effects::loopSound);
		}

		@Override
		public void setMoodSound(Optional<BiomeMoodSound> sound) {
			sound.ifPresent(effects::moodSound);
		}

		@Override
		public void setAdditionsSound(Optional<BiomeAdditionsSound> sound) {
			sound.ifPresent(effects::additionsSound);
		}

		@Override
		public void setMusic(Optional<MusicSound> sound) {
			sound.ifPresent(effects::music);
		}
	}

	private class GenerationSettingsContextImpl implements GenerationSettingsContext {
		private final Registry<ConfiguredCarver<?>> carvers = registries.get(RegistryKeys.CONFIGURED_CARVER);
		private final Registry<PlacedFeature> features = registries.get(RegistryKeys.PLACED_FEATURE);

		private final BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();

		@Override
		public boolean removeFeature(GenerationStep.Feature step, RegistryKey<PlacedFeature> placedFeatureKey) {
			PlacedFeature placedFeature = getEntry(features, placedFeatureKey).value();
			List<RegistryEntry<PlacedFeature>> featureSteps = generationSettings.getFeatures(step);
			return featureSteps.removeIf(feature -> feature.value() == placedFeature);
		}

		@Override
		public void addFeature(GenerationStep.Feature step, RegistryKey<PlacedFeature> entry) {
			generationSettings.feature(step, features.entryOf(entry));
		}

		@Override
		public void addCarver(GenerationStep.Carver step, RegistryKey<ConfiguredCarver<?>> entry) {
			generationSettings.carver(step, carvers.entryOf(entry));
		}

		@Override
		public boolean removeCarver(GenerationStep.Carver step, RegistryKey<ConfiguredCarver<?>> configuredCarverKey) {
			ConfiguredCarver<?> carver = getEntry(carvers, configuredCarverKey).value();
			return generationSettings.getCarvers(step).removeIf(holder -> holder.value() == carver);
		}
	}

	/**
	 * Gets an entry from the given registry, assuming it's a registry loaded from data packs.
	 * Gives more helpful error messages if an entry is missing by checking if the modder
	 * forgot to data-gen the JSONs corresponding to their built-in objects.
	 */
	private static <T> RegistryEntry.Reference<T> getEntry(Registry<T> registry, RegistryKey<T> key) {
		RegistryEntry.Reference<T> entry = registry.getEntry(key).orElse(null);

		if (entry == null) {
			// The key doesn't exist in the data packs
			throw new IllegalArgumentException("Couldn't find registry entry for " + key);
		}

		return entry;
	}

	private class SpawnSettingsContextImpl implements SpawnSettingsContext {
		private final MobSpawnSettingsBuilder spawnSettings = builder.getMobSpawnSettings();

		@Override
		public void setCreatureSpawnProbability(float probability) {
			spawnSettings.creatureSpawnProbability(probability);
		}

		@Override
		public void addSpawn(SpawnGroup spawnGroup, SpawnSettings.SpawnEntry spawnEntry) {
			Objects.requireNonNull(spawnGroup);
			Objects.requireNonNull(spawnEntry);

			spawnSettings.spawn(spawnGroup, spawnEntry);
		}

		@Override
		public boolean removeSpawns(BiPredicate<SpawnGroup, SpawnSettings.SpawnEntry> predicate) {
			boolean anyRemoved = false;
			for (SpawnGroup category : spawnSettings.getSpawnerTypes()) {
				if (spawnSettings.getSpawner(category).removeIf(data -> predicate.test(category, data))) {
					anyRemoved = true;
				}
			}
			return anyRemoved;
		}

		@Override
		public void setSpawnCost(EntityType<?> entityType, double mass, double gravityLimit) {
			Objects.requireNonNull(entityType);
			
			spawnSettings.spawnCost(entityType, mass, gravityLimit);
		}

		@Override
		public void clearSpawnCost(EntityType<?> entityType) {
			((SpawnSettingsBuilderAccessor) spawnSettings).getSpawnCosts().remove(entityType);
		}
	}
}
