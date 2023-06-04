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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HarvestFarmland.class)
public class FarmerVillagerTaskMixin {
	@Nullable
	@Shadow
	private BlockPos aboveFarmlandPos;

	private int fabric_currentInventorySlot = -1;

	@ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SimpleContainer;getItem(I)Lnet/minecraft/world/item/ItemStack;"), index = 0)
	private int fabric_storeCurrentSlot(int slot) {
		return this.fabric_currentInventorySlot = slot;
	}

	@ModifyVariable(method = "tick", at = @At("LOAD"))
	private boolean fabric_useRegistryForPlace(boolean current, ServerLevel serverWorld, Villager villagerEntity, long l) {
		if (current) {
			return true;
		}

		Container simpleInventory = villagerEntity.getInventory();
		ItemStack currentStack = simpleInventory.getItem(this.fabric_currentInventorySlot);

		if (!currentStack.isEmpty() && VillagerPlantableRegistry.contains(currentStack.getItem())) {
			serverWorld.setBlock(this.aboveFarmlandPos, VillagerPlantableRegistry.getPlantState(currentStack.getItem()), 3);
			return true;
		}

		return false;
	}
}
