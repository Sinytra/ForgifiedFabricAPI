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

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.mixin.biome.MobSpawnSettingsBuilderAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeSpecialEffectsBuilder;
import net.minecraftforge.common.world.ClimateSettingsBuilder;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

public class BiomeModificationContextImpl implements BiomeModificationContext {
	private final RegistryAccess registries;
	private final ModifiableBiomeInfo.BiomeInfo.Builder builder;
	private final WeatherContext weather;
	private final EffectsContext effects;
	private final GenerationSettingsContextImpl generationSettings;
	private final SpawnSettingsContextImpl spawnSettings;

	public BiomeModificationContextImpl(RegistryAccess registries, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
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
			color.ifPresent(effects::foliageColorOverride);
		}

		@Override
		public void setGrassColor(Optional<Integer> color) {
			color.ifPresent(effects::grassColorOverride);
		}

		@Override
		public void setGrassColorModifier(@NotNull BiomeSpecialEffects.GrassColorModifier colorModifier) {
			effects.grassColorModifier(colorModifier);
		}

		@Override
		public void setParticleConfig(Optional<AmbientParticleSettings> particleConfig) {
			particleConfig.ifPresent(effects::ambientParticle);
		}

		@Override
		public void setAmbientSound(Optional<Holder<SoundEvent>> sound) {
			sound.ifPresent(effects::ambientLoopSound);
		}

		@Override
		public void setMoodSound(Optional<AmbientMoodSettings> sound) {
			sound.ifPresent(effects::ambientMoodSound);
		}

		@Override
		public void setAdditionsSound(Optional<AmbientAdditionsSettings> sound) {
			sound.ifPresent(effects::ambientAdditionsSound);
		}

		@Override
		public void setMusic(Optional<Music> sound) {
			sound.ifPresent(effects::backgroundMusic);
		}
	}

	private class GenerationSettingsContextImpl implements GenerationSettingsContext {
		private final Registry<ConfiguredWorldCarver<?>> carvers = registries.registryOrThrow(Registries.CONFIGURED_CARVER);
		private final Registry<PlacedFeature> features = registries.registryOrThrow(Registries.PLACED_FEATURE);

		private final BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();

		@Override
		public boolean removeFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey) {
			PlacedFeature placedFeature = getEntry(features, placedFeatureKey).value();
			List<Holder<PlacedFeature>> featureSteps = generationSettings.getFeatures(step);
			return featureSteps.removeIf(feature -> feature.value() == placedFeature);
		}

		@Override
		public void addFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> entry) {
			generationSettings.addFeature(step, features.getHolderOrThrow(entry));
		}

		@Override
		public void addCarver(GenerationStep.Carving step, ResourceKey<ConfiguredWorldCarver<?>> entry) {
			generationSettings.addCarver(step, carvers.getHolderOrThrow(entry));
		}

		@Override
		public boolean removeCarver(GenerationStep.Carving step, ResourceKey<ConfiguredWorldCarver<?>> configuredCarverKey) {
			ConfiguredWorldCarver<?> carver = getEntry(carvers, configuredCarverKey).value();
			return generationSettings.getCarvers(step).removeIf(holder -> holder.value() == carver);
		}
	}

	/**
	 * Gets an entry from the given registry, assuming it's a registry loaded from data packs.
	 * Gives more helpful error messages if an entry is missing by checking if the modder
	 * forgot to data-gen the JSONs corresponding to their built-in objects.
	 */
	private static <T> Holder.Reference<T> getEntry(Registry<T> registry, ResourceKey<T> key) {
		Holder.Reference<T> entry = registry.getHolder(key).orElse(null);

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
			spawnSettings.creatureGenerationProbability(probability);
		}

		@Override
		public void addSpawn(MobCategory spawnGroup, MobSpawnSettings.SpawnerData spawnEntry) {
			Objects.requireNonNull(spawnGroup);
			Objects.requireNonNull(spawnEntry);

			spawnSettings.addSpawn(spawnGroup, spawnEntry);
		}

		@Override
		public boolean removeSpawns(BiPredicate<MobCategory, MobSpawnSettings.SpawnerData> predicate) {
			boolean anyRemoved = false;
			for (MobCategory category : spawnSettings.getSpawnerTypes()) {
				if (spawnSettings.getSpawner(category).removeIf(data -> predicate.test(category, data))) {
					anyRemoved = true;
				}
			}
			return anyRemoved;
		}

		@Override
		public void setSpawnCost(EntityType<?> entityType, double mass, double gravityLimit) {
			Objects.requireNonNull(entityType);
			
			spawnSettings.addMobCharge(entityType, mass, gravityLimit);
		}

		@Override
		public void clearSpawnCost(EntityType<?> entityType) {
			((MobSpawnSettingsBuilderAccessor) spawnSettings).getMobSpawnCosts().remove(entityType);
		}
	}
}
