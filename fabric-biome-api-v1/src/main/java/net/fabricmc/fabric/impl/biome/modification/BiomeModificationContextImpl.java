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

import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeSpecialEffectsBuilder;
import net.neoforged.neoforge.common.world.ClimateSettingsBuilder;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;

public class BiomeModificationContextImpl implements BiomeModificationContext {
	private final RegistryAccess registries;
	private final Biome biome;
	private final ModifiableBiomeInfo.BiomeInfo.Builder builder;
	private final WeatherContext weather;
	private final AttributesContext attributes;
	private final EffectsContextImpl effects;
	private final GenerationSettingsContextImpl generationSettings;
	private final SpawnSettingsContextImpl spawnSettings;

	public BiomeModificationContextImpl(RegistryAccess registries, Biome biome, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		this.registries = registries;
		this.biome = biome;
		this.builder = builder;
		this.weather = new WeatherContextImpl();
		this.attributes = new AttributesContextImpl();
		this.effects = new EffectsContextImpl();
		this.generationSettings = new GenerationSettingsContextImpl();
		this.spawnSettings = new SpawnSettingsContextImpl();
	}

	@Override
	public WeatherContext getWeather() {
		return weather;
	}

	@Override
	public AttributesContext getAttributes() {
		return attributes;
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
	public MobSpawnSettingsContext getMobSpawnSettings() {
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

	private class AttributesContextImpl implements AttributesContext {
		@Override
		public void addAll(EnvironmentAttributeMap map) {
			EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(biome.getAttributes());
			attributes.putAll(map);
			biome.attributes = attributes.build();
		}

		@Override
		public <T> void set(EnvironmentAttribute<T> key, T value) {
			EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(biome.getAttributes());
			attributes.set(key, value);
			biome.attributes = attributes.build();
		}

		@Override
		public <T, M> void setModifier(EnvironmentAttribute<T> key, AttributeModifier<T, M> modifier, M value) {
			EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(biome.getAttributes());
			attributes.modify(key, modifier, value);
			biome.attributes = attributes.build();
		}
	}

	private class EffectsContextImpl implements EffectsContext {
		private final BiomeSpecialEffectsBuilder effects = builder.getSpecialEffects();

		@Override
		public void setFogColor(int color) {
			attributes.set(EnvironmentAttributes.FOG_COLOR, color);
		}

		@Override
		public void setWaterColor(int color) {
			effects.waterColor(color);
		}

		@Override
		public void setWaterFogColor(int color) {
			attributes.set(EnvironmentAttributes.WATER_FOG_COLOR, color);
		}

		@Override
		public void setSkyColor(int color) {
			attributes.set(EnvironmentAttributes.SKY_COLOR, color);
		}

		@Override
		public void setFoliageColorOverride(Optional<Integer> color) {
			effects.foliageColorOverride = Objects.requireNonNull(color);
		}

		@Override
		public void setDryFoliageColorOverride(Optional<Integer> color) {
			effects.dryFoliageColorOverride = Objects.requireNonNull(color);
		}

		@Override
		public void setGrassColorOverride(Optional<Integer> color) {
			effects.grassColorOverride = Objects.requireNonNull(color);
		}

		@Override
		public void setGrassColorModifier(BiomeSpecialEffects.GrassColorModifier colorModifier) {
			effects.grassColorModifier = Objects.requireNonNull(colorModifier);
		}

		@Override
		public void setMusicVolume(float volume) {
			attributes.set(EnvironmentAttributes.MUSIC_VOLUME, volume);
		}
	}

	private class GenerationSettingsContextImpl implements GenerationSettingsContext {
		private final Registry<ConfiguredWorldCarver<?>> carvers = registries.lookupOrThrow(Registries.CONFIGURED_CARVER);
		private final Registry<PlacedFeature> features = registries.lookupOrThrow(Registries.PLACED_FEATURE);
		private final BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();

		@Override
		public boolean removeFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey) {
			PlacedFeature placedFeature = getHolder(features, placedFeatureKey).value();
			List<Holder<PlacedFeature>> featureSteps = generationSettings.getFeatures(step);
			return featureSteps.removeIf(feature -> feature.value() == placedFeature);
		}

		@Override
		public void addFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> entry) {
			generationSettings.addFeature(step, features.getOrThrow(entry));
		}

		@Override
		public void addCarver(ResourceKey<ConfiguredWorldCarver<?>> entry) {
			// We do not need to delay evaluation of this since the registries are already fully built
			generationSettings.addCarver(getHolder(carvers, entry));
		}

		@Override
		public boolean removeCarver(ResourceKey<ConfiguredWorldCarver<?>> configuredCarverKey) {
			ConfiguredWorldCarver<?> carver = getHolder(carvers, configuredCarverKey).value();
			return generationSettings.getCarvers().removeIf(holder -> holder.value() == carver);
		}
	}

	/**
	 * Gets an entry from the given registry, assuming it's a registry loaded from data packs.
	 * Gives more helpful error messages if an entry is missing by checking if the modder
	 * forgot to data-gen the JSONs corresponding to their built-in objects.
	 */
	private static <T> Holder.Reference<T> getHolder(Registry<T> registry, ResourceKey<T> key) {
		Holder.Reference<T> holder = registry.get(key).orElse(null);

		if (holder == null) {
			// The key doesn't exist in the data packs
			throw new IllegalArgumentException("Couldn't find holder for " + key);
		}

		return holder;
	}

	private class SpawnSettingsContextImpl implements MobSpawnSettingsContext {
		private final MobSpawnSettingsBuilder spawnSettings = builder.getMobSpawnSettings();

		@Override
		public void setCreatureGenerationProbability(float probability) {
			spawnSettings.creatureGenerationProbability(probability);
		}

		@Override
		public @UnmodifiableView List<Weighted<MobSpawnSettings.SpawnerData>> getMobs(MobCategory category) {
			Objects.requireNonNull(category);

			return spawnSettings.getSpawner(category).getList();
		}

		@Override
		public void addSpawn(MobCategory category, MobSpawnSettings.SpawnerData data, int weight) {
			Objects.requireNonNull(category);
			Objects.requireNonNull(data);

			spawnSettings.addSpawn(category, weight, data);
		}

		@Override
		public boolean removeSpawns(BiPredicate<MobCategory, MobSpawnSettings.SpawnerData> predicate) {
			boolean anyRemoved = false;

			for (MobCategory group : spawnSettings.getSpawnerTypes()) {
				int oldSize = spawnSettings.getSpawner(group).getList().size();
				spawnSettings.getSpawner(group).removeIf(entry -> predicate.test(group, entry.value()));
				if (oldSize > spawnSettings.getSpawner(group).getList().size()) {
					anyRemoved = true;
				}
			}

			return anyRemoved;
		}

		@Override
		public void addMobCharge(EntityType<?> entityType, double charge, double energyBudget) {
			Objects.requireNonNull(entityType);
			spawnSettings.addMobCharge(entityType, charge, energyBudget);
		}

		@Override
		public void clearMobCharge(EntityType<?> entityType) {
			spawnSettings.removeSpawnCost(entityType);
		}
	}
}
