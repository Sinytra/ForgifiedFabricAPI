package org.sinytra.fabric.transfer_api.compat;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

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
        // Keep a minimum of 1 slot, required for TrashingStorage test to work
        // See VanillaInventoryCodeHooks#isFull
        return Math.max(slots.size(), 1);
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot >= slots.size()) {
            return ItemStack.EMPTY;
        }
        StorageView<ItemVariant> view = slots.get(slot);
        return view != null ? view.getResource().toStack((int) view.getAmount()) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        try (Transaction transaction = Transaction.openOuter()) {
            ItemVariant resource = ItemVariant.of(stack);
            if (resource.isBlank()) return ItemStack.EMPTY;
            int inserted = (int) storage.insert(resource, stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            int remainder = stack.getCount() - inserted;
            return resource.toStack(remainder);
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= slots.size()) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            ItemVariant resource = slots.get(slot).getResource();
            if (!resource.isBlank()) {
                int extracted = (int) storage.extract(resource, amount, transaction);
                if (!simulate && extracted > 0) {
                    transaction.commit();
                }
                return resource.toStack(extracted);
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot >= slots.size() ? 0 : (int) slots.get(slot).getCapacity();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return StorageUtil.simulateInsert(storage, ItemVariant.of(stack), stack.getCount(), null) > 0;
    }
}

