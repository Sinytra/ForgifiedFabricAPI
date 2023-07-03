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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ForgeItemStorage implements Storage<ItemVariant> {
    private final IItemHandler handler;

    public ForgeItemStorage(IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        int inserted = ItemHandlerHelper.insertItem(this.handler, resource.toStack((int) maxAmount), true).getCount();
        transaction.addCloseCallback((context, result) -> {
            if (result.wasCommitted()) {
                ItemHandlerHelper.insertItem(this.handler, resource.toStack((int) maxAmount), false);
            }
        });
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        int extracted = extractItem(this.handler, resource, (int) maxAmount, true).getCount();
        transaction.addCloseCallback((context, result) -> {
            if (result.wasCommitted()) {
                extractItem(this.handler, resource, (int) maxAmount, false);
            }
        });
        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            views.add(new ForgeItemView(handler, i));
        }
        return views.iterator();
    }

    @NotNull
    public static ItemStack extractItem(IItemHandler dest, @NotNull ItemVariant variant, int maxAmount, boolean simulate) {
        if (dest == null)
            return ItemStack.EMPTY;

        int total = 0;
        for (int i = 0; i < dest.getSlots(); i++) {
            ItemStack available = dest.getStackInSlot(i);
            if (ItemHandlerHelper.canItemStacksStack(available, variant.toStack())) {
                total += dest.extractItem(i, maxAmount - total, simulate).getCount();
            }
        }

        return variant.toStack(total);
    }
}
