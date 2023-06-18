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

package net.fabricmc.fabric.test.screenhandler.item;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

final class BagInventory implements ImplementedInventory {
	private final ItemStack stack;
	private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

	BagInventory(ItemStack stack) {
		this.stack = stack;
		CompoundTag tag = stack.getTagElement("Items");

		if (tag != null) {
			ContainerHelper.loadAllItems(tag, items);
		}
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	public void setChanged() {
		CompoundTag tag = stack.getOrCreateTagElement("Items");
		ContainerHelper.saveAllItems(tag, items);
	}
}
