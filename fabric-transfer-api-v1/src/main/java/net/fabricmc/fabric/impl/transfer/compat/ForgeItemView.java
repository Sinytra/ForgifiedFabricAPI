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

package net.fabricmc.fabric.impl.transfer.compat;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public record ForgeItemView(IItemHandler handler, int slot) implements StorageView<ItemVariant> {

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        ItemStack available = getStack();
        if (ItemHandlerHelper.canItemStacksStack(available, resource.toStack())) {
            ItemStack extracted = handler.extractItem(slot, (int) maxAmount, true);
            transaction.addCloseCallback((context, result) -> {
                if (result.wasCommitted()) {
                    handler.extractItem(slot, (int) maxAmount, false);
                }
            });
            return extracted.getCount();
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return getStack().isEmpty();
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(getStack());
    }

    @Override
    public long getAmount() {
        return getStack().getCount();
    }

    @Override
    public long getCapacity() {
        return handler.getSlotLimit(slot);
    }

    private ItemStack getStack() {
        return handler.getStackInSlot(slot);
    }
}
