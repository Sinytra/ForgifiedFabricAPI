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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public class ItemStorageItemHandler implements IItemHandler {
    private final Storage<ItemVariant> storage;
    private final Int2ObjectMap<StorageView<ItemVariant>> slots;

    public ItemStorageItemHandler(Storage<ItemVariant> storage) {
        this.storage = storage;
        this.slots = new Int2ObjectOpenHashMap<>();
        int i = 0;
        for (StorageView<ItemVariant> view : storage) {
            slots.put(i++, view);
        }
    }

    @Override
    public int getSlots() {
        return slots.size();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        StorageView<ItemVariant> view = slots.get(slot);
        return view != null ? view.getResource().toStack((int) view.getAmount()) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        try (Transaction transaction = Transaction.openOuter()) {
            ItemVariant resource = ItemVariant.of(stack);
            int inserted = (int) storage.insert(resource, stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return resource.toStack(inserted);
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        try (Transaction transaction = Transaction.openOuter()) {
            ItemVariant resource = slots.get(slot).getResource();
            int extracted = (int) storage.extract(resource, amount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return resource.toStack(extracted);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return (int) slots.get(slot).getCapacity();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return storage.simulateInsert(ItemVariant.of(stack), stack.getCount(), null) > 0;
    }
}
