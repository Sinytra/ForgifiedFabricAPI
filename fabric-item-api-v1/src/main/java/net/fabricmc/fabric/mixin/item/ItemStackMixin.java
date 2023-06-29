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

package net.fabricmc.fabric.mixin.item;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.fabricmc.fabric.api.item.v1.FabricItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements FabricItemStack {
	@Final
	@Shadow
	private Item item;
	@Shadow
	private int count;
	@Shadow
	private boolean empty;

	/**
	 * Soft-overwrite updateEmptyState to fix <a href="https://bugs.mojang.com/projects/MC/issues/MC-258939">MC-258939</a>.
	 * Cannot hard-overwrite because Lithium contains a similar but insufficient inject.
	 */
	@Inject(method = "updateEmptyState", at = @At("HEAD"), cancellable = true)
	private void updateEmptyState(CallbackInfo ci) {
		this.empty = this.item == null || this.item == Items.AIR || this.count <= 0;
		ci.cancel();
	}
}
