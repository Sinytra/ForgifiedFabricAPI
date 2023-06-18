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

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin allows us to inject arbitrary logic at the beginning of the "apply" phase.
 * Used by the subclass {@link JsonDataLoaderMixin}.
 */
@Mixin(SimplePreparableReloadListener.class)
public class SinglePreparationResourceReloaderMixin {
	// thenAcceptAsync in reload
	@Inject(at = @At("HEAD"), method = "lambda$reload$1")
	private void applyResourceConditions(ResourceManager resourceManager, ProfilerFiller profiler, Object object, CallbackInfo ci) {
		fabric_applyResourceConditions(resourceManager, profiler, object);
	}

	protected void fabric_applyResourceConditions(ResourceManager resourceManager, ProfilerFiller profiler, Object object) {
	}
}
