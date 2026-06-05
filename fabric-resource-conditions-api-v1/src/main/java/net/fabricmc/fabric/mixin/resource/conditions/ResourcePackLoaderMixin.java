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

import java.io.IOException;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.packs.PackResources;

import net.fabricmc.fabric.impl.resource.conditions.OverlayConditionsMetadata;

@Mixin(ResourcePackLoader.class)
public class ResourcePackLoaderMixin {
	@Inject(
			method = "readMeta",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"
			)
	)
	private static void applyOverlayConditions(CallbackInfoReturnable<?> cir,
											   @Local(name = "overlays") List<String> overlays,
											   @Local(name = "primaryResources") PackResources pack
	) throws IOException {
		OverlayConditionsMetadata overlayMetadata = pack.getMetadataSection(OverlayConditionsMetadata.SERIALIZER);

		if (overlayMetadata != null) {
			overlays.addAll(overlayMetadata.appliedOverlays());
		}
	}
}
