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
import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.packs.OverlayMetadataSection;

import net.minecraft.server.packs.PackType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import net.fabricmc.fabric.impl.resource.conditions.OverlayConditionsMetadata;

@Mixin(Pack.class)
public class PackMixin {
	@ModifyVariable(method = "readPackMetadata", at = @At(value = "STORE", ordinal = 0), name = "overlaySet")
	private static List<String> applyOverlayConditions(List<String> overlays,
													   @Local(argsOnly = true) PackType type,
													   @Local(name = "pack") PackResources pack
	) throws IOException {
		// Avoid trying to load Fabric overlays for xplat mods that define both.
		// The condition registry entries would be missing.
		if (pack.getMetadataSection(OverlayMetadataSection.forPackTypeNeoForge(type)) != null) {
			return overlays;
		}

		List<String> appliedOverlays = new ArrayList<>(overlays);
		OverlayConditionsMetadata overlayMetadata = pack.getMetadataSection(OverlayConditionsMetadata.SERIALIZER);

		if (overlayMetadata != null) {
			appliedOverlays.addAll(overlayMetadata.appliedOverlays());
		}

		return List.copyOf(appliedOverlays);
	}
}
