package net.fabricmc.fabric.impl.transfer.compat;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class SlottedItemStorageItemHandler implements IItemHandler {
    private final SlottedStorage<ItemVariant> storage;

    public SlottedItemStorageItemHandler(SlottedStorage<ItemVariant> storage) {
        this.storage = storage;
    }

    @Override
    public int getSlots() {
        return storage.getSlotCount();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        SingleSlotStorage<ItemVariant> storageSlot = storage.getSlot(slot);
        return storageSlot.getResource().toStack((int) storageSlot.getAmount());
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        try (Transaction transaction = Transaction.openOuter()) {
            ItemVariant resource = ItemVariant.of(stack);
            int inserted = (int) storage.getSlot(slot).insert(resource, stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return inserted >= stack.getCount() ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - inserted);
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        try (Transaction transaction = Transaction.openOuter()) {
            SingleSlotStorage<ItemVariant> view = storage.getSlot(slot);
            ItemVariant resource = view.getResource();
            if (!resource.isBlank()) {
                int extracted = (int) view.extract(resource, amount, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return resource.toStack(extracted);
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return (int) storage.getSlot(slot).getCapacity();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return storage.getSlot(slot).simulateInsert(ItemVariant.of(stack), stack.getCount(), null) > 0;
    }
}
