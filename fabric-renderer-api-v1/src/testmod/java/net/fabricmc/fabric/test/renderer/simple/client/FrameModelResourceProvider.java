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

package net.fabricmc.fabric.test.renderer.simple.client;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides the unbaked model for use with the frame block.
 */
final class FrameModelResourceProvider implements ModelResourceProvider {
	static final Set<ResourceLocation> FRAME_MODELS = new HashSet<>();

	@Nullable
	@Override
	public UnbakedModel loadModelResource(ResourceLocation resourceId, ModelProviderContext context) {
		if (FRAME_MODELS.contains(resourceId)) {
			return new FrameUnbakedModel();
		}

		return null;
	}
}
