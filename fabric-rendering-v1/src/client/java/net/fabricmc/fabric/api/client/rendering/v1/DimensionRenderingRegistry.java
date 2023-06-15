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

package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.impl.client.rendering.DimensionRenderingRegistryImpl;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Dimensional renderers render world specific visuals of a world.
 * They may be used to render the sky, weather, or clouds.
 * The {@link DimensionSpecialEffects} is the vanilla dimensional renderer.
 */
public interface DimensionRenderingRegistry {
	/**
	 * Registers the custom sky renderer for a {@link Level}.
	 *
	 * <p>This overrides Vanilla's sky rendering.
	 * @param key A {@link ResourceKey} for your {@link Level}
	 * @param renderer A {@link SkyRenderer} implementation
	 * @throws IllegalArgumentException if key is already registered.
	 */
	static void registerSkyRenderer(ResourceKey<Level> key, SkyRenderer renderer) {
		DimensionRenderingRegistryImpl.registerSkyRenderer(key, renderer);
	}

	/**
	 * Registers a custom weather renderer for a {@link Level}.
	 *
	 * <p>This overrides Vanilla's weather rendering.
	 * @param key A RegistryKey for your {@link Level}
	 * @param renderer A {@link WeatherRenderer} implementation
	 * @throws IllegalArgumentException if key is already registered.
	 */
	static void registerWeatherRenderer(ResourceKey<Level> key, WeatherRenderer renderer) {
		DimensionRenderingRegistryImpl.registerWeatherRenderer(key, renderer);
	}

	/**
	 * Registers dimension effects for an {@link ResourceLocation}.
	 *
	 * <p>This registers a new option for the "effects" entry of the dimension type json.
	 *
	 * @param key     The {@link ResourceLocation} for the new option entry.
	 * @param effects The {@link DimensionSpecialEffects} option.
	 * @throws IllegalArgumentException if key is already registered.
	 */
	static void registerDimensionEffects(ResourceLocation key, DimensionSpecialEffects effects) {
		DimensionRenderingRegistryImpl.registerDimensionEffects(key, effects);
	}

	/**
	 * Registers a custom cloud renderer for a {@link Level}.
	 *
	 * <p>This overrides Vanilla's cloud rendering.
	 *
	 * @param key      A {@link ResourceKey} for your {@link Level}
	 * @param renderer A {@link CloudRenderer} implementation
	 * @throws IllegalArgumentException if key is already registered.
	 */
	static void registerCloudRenderer(ResourceKey<Level> key, CloudRenderer renderer) {
		DimensionRenderingRegistryImpl.registerCloudRenderer(key, renderer);
	}

	/**
	 * Gets the custom sky renderer for the given {@link Level}.
	 *
	 * @param key A {@link ResourceKey} for your {@link Level}
	 * @return {@code null} if no custom sky renderer is registered for the dimension.
	 */
	@Nullable
	static SkyRenderer getSkyRenderer(ResourceKey<Level> key) {
		return DimensionRenderingRegistryImpl.getSkyRenderer(key);
	}

	/**
	 * Gets the custom cloud renderer for the given {@link Level}.
	 *
	 * @param key A {@link ResourceKey} for your {@link Level}
	 * @return {@code null} if no custom cloud renderer is registered for the dimension.
	 */
	@Nullable
	static CloudRenderer getCloudRenderer(ResourceKey<Level> key) {
		return DimensionRenderingRegistryImpl.getCloudRenderer(key);
	}

	/**
	 * Gets the custom weather effect renderer for the given {@link Level}.
	 *
	 * @return {@code null} if no custom weather effect renderer is registered for the dimension.
	 */
	@Nullable
	static WeatherRenderer getWeatherRenderer(ResourceKey<Level> key) {
		return DimensionRenderingRegistryImpl.getWeatherRenderer(key);
	}

	/**
	 * Gets the dimension effects registered for an id.
	 * @param key A {@link ResourceKey} for your {@link Level}.
	 * @return overworld effect if no dimension effects is registered for the key.
	 */
	@Nullable
	static DimensionSpecialEffects getDimensionEffects(ResourceLocation key) {
		return DimensionRenderingRegistryImpl.getDimensionEffects(key);
	}

	@FunctionalInterface
	interface SkyRenderer {
		void render(WorldRenderContext context);
	}

	@FunctionalInterface
	interface WeatherRenderer {
		void render(WorldRenderContext context);
	}

	@FunctionalInterface
	interface CloudRenderer {
		void render(WorldRenderContext context);
	}
}
