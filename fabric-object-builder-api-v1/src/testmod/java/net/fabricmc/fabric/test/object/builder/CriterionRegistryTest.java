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

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.resources.ResourceLocation;

public final class CriterionRegistryTest {
	public static void init() {
		CriteriaTriggers.register(new CustomCriterion());
	}

	static class CustomCriterion extends ImpossibleTrigger {
		static final ResourceLocation ID = ObjectBuilderTestConstants.id("custom");

		@Override
		public ResourceLocation getId() {
			return ID;
		}

		@Override
		public ImpossibleTrigger.TriggerInstance createInstance(JsonObject jsonObject, DeserializationContext advancementEntityPredicateDeserializer) {
			ObjectBuilderTestConstants.LOGGER.info("Loading custom criterion in advancement!");
			return super.createInstance(jsonObject, advancementEntityPredicateDeserializer);
		}
	}
}
