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

package net.fabricmc.fabric.api.biome.v1;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Allows {@link Biome} properties to be modified.
 *
 * <p><b>Experimental feature</b>, may be removed or changed without further notice.
 */
public interface BiomeModificationContext {
	/**
	 * Returns the modification context for the biomes weather properties.
	 */
	WeatherContext getWeather();

	/**
	 * Returns the modification context for the biomes effects.
	 */
	EffectsContext getEffects();

	/**
	 * Returns the modification context for the biomes generation settings.
	 */
	GenerationSettingsContext getGenerationSettings();

	/**
	 * Returns the modification context for the biomes spawn settings.
	 */
	SpawnSettingsContext getSpawnSettings();

	interface WeatherContext {
		/**
		 * @see Biome#getPrecipitationAt(BlockPos)
		 * @see Biome.BiomeBuilder#hasPrecipitation(boolean)
		 */
		void setPrecipitation(boolean hasPrecipitation);

		/**
		 * @see Biome#getBaseTemperature()
		 * @see Biome.BiomeBuilder#temperature(float)
		 */
		void setTemperature(float temperature);

		/**
		 * @see Biome.BiomeBuilder#temperatureAdjustment(Biome.TemperatureModifier)
		 */
		void setTemperatureModifier(Biome.TemperatureModifier temperatureModifier);

		/**
		 * @see Biome#getModifiedClimateSettings()
		 * @see Biome.BiomeBuilder#downfall(float)
		 */
		void setDownfall(float downfall);
	}

	interface EffectsContext {
		/**
		 * @see BiomeSpecialEffects#getFogColor()
		 * @see BiomeSpecialEffects.Builder#fogColor(int)
		 */
		void setFogColor(int color);

		/**
		 * @see BiomeSpecialEffects#getWaterColor()
		 * @see BiomeSpecialEffects.Builder#waterColor(int)
		 */
		void setWaterColor(int color);

		/**
		 * @see BiomeSpecialEffects#getWaterFogColor()
		 * @see BiomeSpecialEffects.Builder#waterFogColor(int)
		 */
		void setWaterFogColor(int color);

		/**
		 * @see BiomeSpecialEffects#getSkyColor()
		 * @see BiomeSpecialEffects.Builder#skyColor(int)
		 */
		void setSkyColor(int color);

		/**
		 * @see BiomeSpecialEffects#getFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		void setFoliageColor(Optional<Integer> color);

		/**
		 * @see BiomeSpecialEffects#getFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		default void setFoliageColor(int color) {
			setFoliageColor(Optional.of(color));
		}

		/**
		 * @see BiomeSpecialEffects#getFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		default void setFoliageColor(OptionalInt color) {
			color.ifPresentOrElse(this::setFoliageColor, this::clearFoliageColor);
		}

		/**
		 * @see BiomeSpecialEffects#getFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		default void clearFoliageColor() {
			setFoliageColor(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#getGrassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		void setGrassColor(Optional<Integer> color);

		/**
		 * @see BiomeSpecialEffects#getGrassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		default void setGrassColor(int color) {
			setGrassColor(Optional.of(color));
		}

		/**
		 * @see BiomeSpecialEffects#getGrassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		default void setGrassColor(OptionalInt color) {
			color.ifPresentOrElse(this::setGrassColor, this::clearGrassColor);
		}

		/**
		 * @see BiomeSpecialEffects#getGrassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		default void clearGrassColor() {
			setGrassColor(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#getGrassColorModifier()
		 * @see BiomeSpecialEffects.Builder#grassColorModifier(BiomeSpecialEffects.GrassColorModifier)
		 */
		void setGrassColorModifier(@NotNull BiomeSpecialEffects.GrassColorModifier colorModifier);

		/**
		 * @see BiomeSpecialEffects#getAmbientParticleSettings()
		 * @see BiomeSpecialEffects.Builder#ambientParticle(AmbientParticleSettings)
		 */
		void setParticleConfig(Optional<AmbientParticleSettings> particleConfig);

		/**
		 * @see BiomeSpecialEffects#getAmbientParticleSettings()
		 * @see BiomeSpecialEffects.Builder#ambientParticle(AmbientParticleSettings)
		 */
		default void setParticleConfig(@NotNull AmbientParticleSettings particleConfig) {
			setParticleConfig(Optional.of(particleConfig));
		}

		/**
		 * @see BiomeSpecialEffects#getAmbientParticleSettings()
		 * @see BiomeSpecialEffects.Builder#ambientParticle(AmbientParticleSettings)
		 */
		default void clearParticleConfig() {
			setParticleConfig(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#getAmbientParticleSettings()
		 * @see BiomeSpecialEffects.Builder#ambientLoopSound(Holder)
		 */
		void setAmbientSound(Optional<Holder<SoundEvent>> sound);

		/**
		 * @see BiomeSpecialEffects#getAmbientParticleSettings()
		 * @see BiomeSpecialEffects.Builder#ambientLoopSound(Holder)
		 */
		default void setAmbientSound(@NotNull Holder<SoundEvent> sound) {
			setAmbientSound(Optional.of(sound));
		}

		/**
		 * @see BiomeSpecialEffects#getAmbientParticleSettings()
		 * @see BiomeSpecialEffects.Builder#ambientLoopSound(Holder)
		 */
		default void clearAmbientSound() {
			setAmbientSound(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#getAmbientMoodSettings()
		 * @see BiomeSpecialEffects.Builder#ambientMoodSound(AmbientMoodSettings)
		 */
		void setMoodSound(Optional<AmbientMoodSettings> sound);

		/**
		 * @see BiomeSpecialEffects#getAmbientMoodSettings()
		 * @see BiomeSpecialEffects.Builder#ambientMoodSound(AmbientMoodSettings)
		 */
		default void setMoodSound(@NotNull AmbientMoodSettings sound) {
			setMoodSound(Optional.of(sound));
		}

		/**
		 * @see BiomeSpecialEffects#getAmbientMoodSettings()
		 * @see BiomeSpecialEffects.Builder#ambientMoodSound(AmbientMoodSettings)
		 */
		default void clearMoodSound() {
			setMoodSound(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#getAmbientAdditionsSettings()
		 * @see BiomeSpecialEffects.Builder#ambientAdditionsSound(AmbientAdditionsSettings)
		 */
		void setAdditionsSound(Optional<AmbientAdditionsSettings> sound);

		/**
		 * @see BiomeSpecialEffects#getAmbientAdditionsSettings()
		 * @see BiomeSpecialEffects.Builder#ambientAdditionsSound(AmbientAdditionsSettings)
		 */
		default void setAdditionsSound(@NotNull AmbientAdditionsSettings sound) {
			setAdditionsSound(Optional.of(sound));
		}

		/**
		 * @see BiomeSpecialEffects#getAmbientAdditionsSettings()
		 * @see BiomeSpecialEffects.Builder#ambientAdditionsSound(AmbientAdditionsSettings)
		 */
		default void clearAdditionsSound() {
			setAdditionsSound(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#getBackgroundMusic()
		 * @see BiomeSpecialEffects.Builder#backgroundMusic(Music)
		 */
		void setMusic(Optional<Music> sound);

		/**
		 * @see BiomeSpecialEffects#getBackgroundMusic()
		 * @see BiomeSpecialEffects.Builder#backgroundMusic(Music)
		 */
		default void setMusic(@NotNull Music sound) {
			setMusic(Optional.of(sound));
		}

		/**
		 * @see BiomeSpecialEffects#getBackgroundMusic()
		 * @see BiomeSpecialEffects.Builder#backgroundMusic(Music)
		 */
		default void clearMusic() {
			setMusic(Optional.empty());
		}
	}

	interface GenerationSettingsContext {
		/**
		 * Removes a feature from one of this biomes generation steps, and returns if any features were removed.
		 */
		boolean removeFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey);

		/**
		 * Removes a feature from all of this biomes generation steps, and returns if any features were removed.
		 */
		default boolean removeFeature(ResourceKey<PlacedFeature> placedFeatureKey) {
			boolean anyFound = false;

			for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
				if (removeFeature(step, placedFeatureKey)) {
					anyFound = true;
				}
			}

			return anyFound;
		}

		/**
		 * Adds a feature to one of this biomes generation steps, identified by the placed feature's registry key.
		 */
		void addFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey);

		/**
		 * Adds a configured carver to one of this biomes generation steps.
		 */
		void addCarver(GenerationStep.Carving step, ResourceKey<ConfiguredWorldCarver<?>> carverKey);

		/**
		 * Removes all carvers with the given key from one of this biomes generation steps.
		 *
		 * @return True if any carvers were removed.
		 */
		boolean removeCarver(GenerationStep.Carving step, ResourceKey<ConfiguredWorldCarver<?>> ConfiguredWorldCarverKey);

		/**
		 * Removes all carvers with the given key from all of this biomes generation steps.
		 *
		 * @return True if any carvers were removed.
		 */
		default boolean removeCarver(ResourceKey<ConfiguredWorldCarver<?>> ConfiguredWorldCarverKey) {
			boolean anyFound = false;

			for (GenerationStep.Carving step : GenerationStep.Carving.values()) {
				if (removeCarver(step, ConfiguredWorldCarverKey)) {
					anyFound = true;
				}
			}

			return anyFound;
		}
	}

	interface SpawnSettingsContext {
		/**
		 * Associated JSON property: <code>creature_spawn_probability</code>.
		 *
		 * @see MobSpawnSettings#getCreatureProbability()
		 * @see MobSpawnSettings.Builder#creatureGenerationProbability(float)
		 */
		void setCreatureSpawnProbability(float probability);

		/**
		 * Associated JSON property: <code>spawners</code>.
		 *
		 * @see MobSpawnSettings#getMobs(MobCategory)
		 * @see MobSpawnSettings.Builder#addSpawn(MobCategory, MobSpawnSettings.SpawnerData)
		 */
		void addSpawn(MobCategory MobCategory, MobSpawnSettings.SpawnerData spawnEntry);

		/**
		 * Removes any spawns matching the given predicate from this biome, and returns true if any matched.
		 *
		 * <p>Associated JSON property: <code>spawners</code>.
		 */
		boolean removeSpawns(BiPredicate<MobCategory, MobSpawnSettings.SpawnerData> predicate);

		/**
		 * Removes all spawns of the given entity type.
		 *
		 * <p>Associated JSON property: <code>spawners</code>.
		 *
		 * @return True if any spawns were removed.
		 */
		default boolean removeSpawnsOfEntityType(EntityType<?> entityType) {
			return removeSpawns((MobCategory, spawnEntry) -> spawnEntry.type == entityType);
		}

		/**
		 * Removes all spawns of the given spawn group.
		 *
		 * <p>Associated JSON property: <code>spawners</code>.
		 */
		default void clearSpawns(MobCategory group) {
			removeSpawns((MobCategory, spawnEntry) -> MobCategory == group);
		}

		/**
		 * Removes all spawns.
		 *
		 * <p>Associated JSON property: <code>spawners</code>.
		 */
		default void clearSpawns() {
			removeSpawns((MobCategory, spawnEntry) -> true);
		}

		/**
		 * Associated JSON property: <code>spawn_costs</code>.
		 *
		 * @see MobSpawnSettings#getMobSpawnCost(EntityType)
		 * @see MobSpawnSettings.Builder#addMobCharge(EntityType, double, double)
		 */
		void setSpawnCost(EntityType<?> entityType, double mass, double gravityLimit);

		/**
		 * Removes a spawn cost entry for a given entity type.
		 *
		 * <p>Associated JSON property: <code>spawn_costs</code>.
		 */
		void clearSpawnCost(EntityType<?> entityType);
	}
}
