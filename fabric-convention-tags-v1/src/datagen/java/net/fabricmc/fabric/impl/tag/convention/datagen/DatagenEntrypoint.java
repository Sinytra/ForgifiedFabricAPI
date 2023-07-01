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

package net.fabricmc.fabric.impl.tag.convention.datagen;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.impl.tag.convention.ConventionTagsImpl;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.BiomeTagGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.BlockTagGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.EnchantmentTagGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.EntityTypeTagGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.FluidTagGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.ItemTagGenerator;

@Mod("fabric_convention_tags_v1_datagen")
public class DatagenEntrypoint {

	public DatagenEntrypoint() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(DatagenEntrypoint::onGatherData);
	}

	private static void onGatherData(GatherDataEvent event) {
		final FabricDataGenerator fabricDataGenerator = FabricDataGenerator.create(ConventionTagsImpl.MOD_ID, event);
		final FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		BlockTagGenerator blockTags = pack.addProvider(BlockTagGenerator::new);
		pack.addProvider((output, wrapperLookup) -> new ItemTagGenerator(output, wrapperLookup, blockTags));
		pack.addProvider(FluidTagGenerator::new);
		pack.addProvider(EnchantmentTagGenerator::new);
		pack.addProvider(BiomeTagGenerator::new);
		pack.addProvider(EntityTypeTagGenerator::new);
	}
}
