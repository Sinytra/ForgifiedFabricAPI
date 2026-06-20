package net.fabricmc.fabric.impl.transfer.compat;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class NeoItemSingleSlotStorage implements SingleSlotStorage<ItemVariant> {
	private final ResourceHandler<ItemResource> inner;
	private final int slot;

	public NeoItemSingleSlotStorage(ResourceHandler<ItemResource> inner, int slot) {
		this.inner = inner;
		this.slot = slot;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.inner.insert(
				this.slot,
				TransferCompatUtil.toResource(resource),
				Ints.saturatedCast(maxAmount),
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.inner.extract(
				this.slot,
				TransferCompatUtil.toResource(resource),
				Ints.saturatedCast(maxAmount),
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}

	@Override
	public boolean isResourceBlank() {
		return this.inner.getResource(this.slot).isEmpty();
	}

	@Override
	public ItemVariant getResource() {
		return TransferCompatUtil.toVariant(this.inner.getResource(this.slot));
	}

	@Override
	public long getAmount() {
		return this.inner.getAmountAsLong(this.slot);
	}

	@Override
	public long getCapacity() {
		return this.inner.getCapacityAsLong(this.slot, this.inner.getResource(this.slot));
	}
}
