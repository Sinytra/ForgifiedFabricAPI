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

package net.fabricmc.fabric.mixin.content.registry;

import net.fabricmc.fabric.api.registry.VillagerPlantableRegistry;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(Villager.class)
public class VillagerEntityMixin {
	@Redirect(method = "hasFarmSeeds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SimpleContainer;hasAnyOf(Ljava/util/Set;)Z"))
	private boolean fabric_useRegistry(SimpleContainer inventory, Set<Item> items) {
		return inventory.hasAnyOf(VillagerPlantableRegistry.getItems());
	}
}
