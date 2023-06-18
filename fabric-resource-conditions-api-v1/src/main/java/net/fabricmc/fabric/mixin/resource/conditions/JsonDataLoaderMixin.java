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

package net.fabricmc.fabric.mixin.resource.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;

/**
 * Using {@link SinglePreparationResourceReloaderMixin}, apply resource conditions at the very beginning of the "apply" phase.
 */
@Mixin(SimpleJsonResourceReloadListener.class)
public class JsonDataLoaderMixin extends SinglePreparationResourceReloaderMixin {
	@Shadow
	@Final
	private String directory;

	@Override
	@SuppressWarnings("unchecked")
	protected void fabric_applyResourceConditions(ResourceManager resourceManager, ProfilerFiller profiler, Object object) {
		profiler.push("Fabric resource conditions: %s".formatted(directory));

		Iterator<Map.Entry<ResourceLocation, JsonElement>> it = ((Map<ResourceLocation, JsonElement>) object).entrySet().iterator();
		boolean debugLogEnabled = ResourceConditionsImpl.LOGGER.isDebugEnabled();

		while (it.hasNext()) {
			Map.Entry<ResourceLocation, JsonElement> entry = it.next();
			JsonElement resourceData = entry.getValue();

			if (resourceData.isJsonObject()) {
				JsonObject obj = resourceData.getAsJsonObject();

				if (obj.has(ResourceConditions.CONDITIONS_KEY)) {
					boolean matched = ResourceConditions.objectMatchesConditions(obj);

					if (!matched) {
						it.remove();
					}

					if (debugLogEnabled) {
						String verdict = matched ? "Allowed" : "Rejected";
						ResourceConditionsImpl.LOGGER.debug("{} resource of type {} with id {}", verdict, directory, entry.getKey());
					}
				}
			}
		}

		profiler.pop();
	}
}
