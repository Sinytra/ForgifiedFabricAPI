package net.fabricmc.fabric.impl.transfer.compat;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;

public class FabricSlottedItemResourceHandler implements ResourceHandler<ItemResource> {
	private final SlottedStorage<ItemVariant> inner;

	public FabricSlottedItemResourceHandler(SlottedStorage<ItemVariant> inner) {
		this.inner = inner;
	}

	@Override
	public int size() {
		return this.inner.getSlotCount();
	}

	@Override
	public ItemResource getResource(int index) {
		return TransferCompatUtil.toResource(this.inner.getSlot(index).getResource());
	}

	@Override
	public long getAmountAsLong(int index) {
		return this.inner.getSlot(index).getAmount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return this.inner.getSlot(index).getCapacity();
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return StorageUtil.simulateInsert(
				this.inner.getSlot(index),
				TransferCompatUtil.toVariant(resource),
				resource.getMaxStackSize(),
				null
		) > 0;
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		return Ints.saturatedCast(
				this.inner.getSlot(index).insert(
						TransferCompatUtil.toVariant(resource),
						amount,
						TransferCompatUtil.toFabricCtx(transaction)
				)
		);
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		return Ints.saturatedCast(
				this.inner.getSlot(index).extract(
						TransferCompatUtil.toVariant(resource),
						amount,
						TransferCompatUtil.toFabricCtx(transaction)
				)
		);
	}
}
