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

package net.fabricmc.fabric.mixin.itemgroup;

import net.fabricmc.fabric.api.itemgroup.v1.IdentifiableItemGroup;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroup;
import net.fabricmc.fabric.impl.itemgroup.MinecraftItemGroups;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(CreativeModeTab.class)
abstract class ItemGroupMixin implements IdentifiableItemGroup, FabricItemGroup {
	@Unique
	private ResourceLocation identifier;

	@Override
	public ResourceLocation getId() {
		final ResourceLocation forgeTabId = CreativeModeTabRegistry.getName((CreativeModeTab) (Object) this);
		if (forgeTabId != null) {
			final ResourceLocation fabricVanillaTabId = MinecraftItemGroups.FORGE_ID_MAP.get(forgeTabId);
			
			return fabricVanillaTabId != null ? fabricVanillaTabId : forgeTabId;
		}
		
		final ResourceLocation fabricVanillaTabId = MinecraftItemGroups.FABRIC_ID_MAP.get((CreativeModeTab) (Object) this);
		if (fabricVanillaTabId != null) {
			return fabricVanillaTabId;
		}

		// No id known, generate a random one
		if (identifier == null) {
			setId(new ResourceLocation("minecraft", "unidentified_" + UUID.randomUUID()));
		}

		return identifier;
	}

	@Override
	public void setId(ResourceLocation identifier) {
		if (this.identifier != null) {
			throw new IllegalStateException("Cannot set id to (%s) as item group already has id (%s)".formatted(identifier, this.identifier));
		}

		this.identifier = identifier;
	}
}
