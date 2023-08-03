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

import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.registry.*;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class FabricDataGenHelper {
	/**
	 * When enabled providers can enable extra validation, such as ensuring all registry entries have data generated for them.
	 */
	public static final boolean STRICT_VALIDATION = System.getProperty("fabric-api.datagen.strict-validation") != null;

	private FabricDataGenHelper() {
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
