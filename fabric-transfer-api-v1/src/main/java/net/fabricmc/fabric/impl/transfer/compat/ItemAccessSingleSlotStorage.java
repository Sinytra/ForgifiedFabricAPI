package net.fabricmc.fabric.impl.transfer.compat;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ItemAccessSingleSlotStorage implements SingleSlotStorage<ItemVariant> {
	private final ItemAccess inner;

	public ItemAccessSingleSlotStorage(ItemAccess inner) {
		this.inner = inner;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.inner.insert(
				TransferCompatUtil.toResource(resource),
				Ints.saturatedCast(maxAmount),
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.inner.extract(
				TransferCompatUtil.toResource(resource),
				Ints.saturatedCast(maxAmount),
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}

	@Override
	public boolean isResourceBlank() {
		return this.inner.getResource().isEmpty();
	}

	@Override
	public ItemVariant getResource() {
		return TransferCompatUtil.toVariant(this.inner.getResource());
	}

	@Override
	public long getAmount() {
		return this.inner.getAmount();
	}

	@Override
	public long getCapacity() {
		return this.inner.getResource().getMaxStackSize();
	}
}
