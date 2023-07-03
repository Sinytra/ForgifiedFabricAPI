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
