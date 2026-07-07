package net.fabricmc.fabric.impl.transfer.compat;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;

public class FabricSlottedFluidResourceHandler implements ResourceHandler<FluidResource> {
	private final SlottedStorage<FluidVariant> inner;

	public FabricSlottedFluidResourceHandler(SlottedStorage<FluidVariant> inner) {
		this.inner = inner;
	}

	@Override
	public int size() {
		return this.inner.getSlotCount();
	}

	@Override
	public FluidResource getResource(int index) {
		return TransferCompatUtil.toResource(this.inner.getSlot(index).getResource());
	}

	@Override
	public long getAmountAsLong(int index) {
		return TransferCompatUtil.toNeoBucketLong(this.inner.getSlot(index).getAmount());
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		return TransferCompatUtil.toNeoBucketLong(this.inner.getSlot(index).getCapacity());
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return StorageUtil.simulateInsert(
				this.inner.getSlot(index),
				TransferCompatUtil.toVariant(resource),
				1,
				null
		) > 0;
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		long inserted = this.inner.getSlot(index).insert(
				TransferCompatUtil.toVariant(resource),
				TransferCompatUtil.toFabricBucket(amount),
				TransferCompatUtil.toFabricCtx(transaction)
		);
		return TransferCompatUtil.toNeoBucket(inserted);
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		long extracted = this.inner.getSlot(index).extract(
				TransferCompatUtil.toVariant(resource),
				TransferCompatUtil.toFabricBucket(amount),
				TransferCompatUtil.toFabricCtx(transaction)
		);
		return TransferCompatUtil.toNeoBucket(extracted);
	}
}
