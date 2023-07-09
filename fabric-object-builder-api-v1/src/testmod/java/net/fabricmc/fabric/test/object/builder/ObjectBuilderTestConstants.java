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

package net.fabricmc.fabric.test.object.builder;

import net.fabricmc.fabric.test.object.builder.client.TealSignClientTest;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import net.minecraft.util.Identifier;

@Mod(ObjectBuilderTestConstants.MOD_ID)
public final class ObjectBuilderTestConstants {
	public static final String MOD_ID = "fabric_object_builder_api_v1_testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String name) {
		return new Identifier(MOD_ID, name);
	}

	public ObjectBuilderTestConstants() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		if (FMLLoader.getDist() == Dist.CLIENT) {
			bus.addListener(TealSignClientTest::onInitializeClient);
		}
		FabricBlockSettingsTest.onInitialize();
		BlockEntityTypeBuilderTest.onInitialize(bus);
		CriterionRegistryTest.init();
		TealSignTest.onInitialize(bus);
		VillagerTypeTest1.onInitialize();
		VillagerTypeTest2.onInitialize();
	}
}
