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

import java.util.Set;

import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import net.minecraft.data.DataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

public class DataGeneratorEntrypoint {
	public static void onGatherData(GatherDataEvent event) {
		final DataGenerator dataGenerator = event.getGenerator();
		final IModInfo modInfo = ModList.get().getModContainerById(FabricBiomeTest.MOD_ID).orElseThrow().getModInfo();
		final FabricDataGenerator fabricDataGenerator = new FabricDataGenerator(dataGenerator, dataGenerator.getPackOutput().getPath(), modInfo, FabricDataGenHelper.STRICT_VALIDATION, event.getLookupProvider());

		onInitializeDataGenerator(fabricDataGenerator);
	}

	public static void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FabricDataGenerator.Pack pack = dataGenerator.createPack();
		pack.addProvider(WorldgenProvider::new);
		pack.addProvider(TestBiomeTagProvider::new);
		pack.addProvider((output, registries) -> new DatapackBuiltinEntriesProvider(output, registries, new RegistryBuilder().addRegistry(RegistryKeys.BIOME, TestBiomes::bootstrap), Set.of(FabricBiomeTest.NAMESPACE)));
	}
}
