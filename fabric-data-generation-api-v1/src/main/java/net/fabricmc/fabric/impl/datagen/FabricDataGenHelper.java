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

package net.fabricmc.fabric.impl.datagen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.data.DataProvider;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;

public final class FabricDataGenHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricDataGenHelper.class);

	/**
	 * When enabled providers can enable extra validation, such as ensuring all registry entries have data generated for them.
	 */
	public static final boolean STRICT_VALIDATION = System.getProperty("fabric-api.datagen.strict-validation") != null;

	private static final Map<ModContainer, DataGeneratorEntrypoint> DATAGEN_ENTRYPOINTS = new HashMap<>();

	public static void registerDatagenEntrypoint(DataGeneratorEntrypoint entrypoint) {
		ModContainer modid = ModLoadingContext.get().getActiveContainer();
		DATAGEN_ENTRYPOINTS.put(modid, entrypoint);
	}
	
	private FabricDataGenHelper() {
	}

	static void onGatherData(GatherDataEvent event) {
		Object2IntOpenHashMap<String> jsonKeySortOrders = (Object2IntOpenHashMap<String>) DataProvider.JSON_KEY_SORT_ORDER;
		Object2IntOpenHashMap<String> defaultJsonKeySortOrders = new Object2IntOpenHashMap<>(jsonKeySortOrders);

		DATAGEN_ENTRYPOINTS.forEach((modContainer, entrypoint) -> {
			LOGGER.info("Running data generator for {}", modContainer.getModId());

			try {
				final String effectiveModId = entrypoint.getEffectiveModId();
				IModInfo modInfo = modContainer.getModInfo();
				HashSet<String> keys = new HashSet<>();
				entrypoint.addJsonKeySortOrders((key, value) -> {
					Objects.requireNonNull(key, "Tried to register a priority for a null key");
					jsonKeySortOrders.put(key, value);
					keys.add(key);
				});

				if (effectiveModId != null) {
					modInfo = ModList.get().getModContainerById(effectiveModId).map(ModContainer::getModInfo).orElseThrow(() -> new RuntimeException("Failed to find effective mod container for mod id (%s)".formatted(effectiveModId)));
				}

				final RegistryBuilder builder = new RegistryBuilder();
				entrypoint.buildRegistry(builder);

				FabricDataGenerator dataGenerator = FabricDataGenerator.create(modInfo, event, builder);
				entrypoint.onInitializeDataGenerator(dataGenerator);

				jsonKeySortOrders.keySet().removeAll(keys);
				jsonKeySortOrders.putAll(defaultJsonKeySortOrders);
			} catch (Throwable t) {
				throw new RuntimeException("Failed to run data generator from mod (%s)".formatted(modContainer.getModId()), t);
			}
		});
	}

	public static RegistryWrapper.WrapperLookup createRegistryWrapper(RegistryWrapper.WrapperLookup original, RegistryBuilder registryBuilder) {
		// Build a list of all the RegistryBuilder's including vanilla's
		List<RegistryBuilder> builders = new ArrayList<>();
		builders.add(BuiltinRegistries.REGISTRY_BUILDER);

		builders.add(registryBuilder);

		// Collect all the bootstrap functions, and merge the lifecycles.
		class BuilderData {
			final RegistryKey key;
			final List<RegistryBuilder.BootstrapFunction<?>> bootstrapFunctions;
			Lifecycle lifecycle;

			BuilderData(RegistryKey key) {
				this.key = key;
				this.bootstrapFunctions = new ArrayList<>();
				this.lifecycle = null;
			}

			void with(RegistryBuilder.RegistryInfo<?> registryInfo) {
				bootstrapFunctions.add(registryInfo.bootstrap());
				lifecycle = registryInfo.lifecycle().add(lifecycle);
			}

			void apply(RegistryBuilder builder) {
				builder.addRegistry(key, lifecycle, this::bootstrap);
			}

			void bootstrap(Registerable registerable) {
				for (RegistryBuilder.BootstrapFunction<?> function : bootstrapFunctions) {
					function.run(registerable);
				}
			}
		}

		Map<RegistryKey<?>, BuilderData> builderDataMap = new HashMap<>();

		for (RegistryBuilder builder : builders) {
			for (RegistryBuilder.RegistryInfo<?> info : builder.registries) {
				builderDataMap.computeIfAbsent(info.key(), BuilderData::new)
						.with(info);
			}
		}

		// Apply all the builders into one.
		RegistryBuilder merged = new RegistryBuilder();

		for (BuilderData value : builderDataMap.values()) {
			value.apply(merged);
		}
		var builderKeys = new HashSet<>(merged.getEntryKeys());
		DataPackRegistriesHooks.getDataPackRegistriesWithDimensions().filter(data -> !builderKeys.contains(data.key())).forEach(data -> merged.addRegistry(data.key(), context -> {}));

		RegistryWrapper.WrapperLookup wrapperLookup = merged.createWrapperLookup(DynamicRegistryManager.of(Registries.REGISTRIES), original);
		BuiltinRegistries.validate(wrapperLookup);
		return wrapperLookup;
	}

	/**
	 * Used to keep track of conditions associated to generated objects.
	 */
	private static final Map<Object, ConditionJsonProvider[]> CONDITIONS_MAP = new IdentityHashMap<>();

	public static void addConditions(Object object, ConditionJsonProvider[] conditions) {
		CONDITIONS_MAP.merge(object, conditions, ArrayUtils::addAll);
	}

	@Nullable
	public static ConditionJsonProvider[] consumeConditions(Object object) {
		return CONDITIONS_MAP.remove(object);
	}
}
