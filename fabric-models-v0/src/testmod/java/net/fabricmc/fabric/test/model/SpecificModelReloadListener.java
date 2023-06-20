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

package net.fabricmc.fabric.test.model;

import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.List;

public class SpecificModelReloadListener extends SimplePreparableReloadListener<Unit> implements IdentifiableResourceReloadListener {
	public static final SpecificModelReloadListener INSTANCE = new SpecificModelReloadListener();
	public static final ResourceLocation ID = new ResourceLocation(ModelTestModClient.MODID, "specific_model");

	private BakedModel specificModel;

	public BakedModel getSpecificModel() {
		return specificModel;
	}

	@Override
	protected Unit prepare(ResourceManager manager, ProfilerFiller profiler) {
		return Unit.INSTANCE;
	}

	@Override
	protected void apply(Unit loader, ResourceManager manager, ProfilerFiller profiler) {
		specificModel = BakedModelManagerHelper.getModel(Minecraft.getInstance().getModelManager(), ModelTestModClient.MODEL_ID);
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return List.of(ResourceReloadListenerKeys.MODELS);
	}
}
