package net.fabricmc.fabric.impl.transfer.compat;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class NeoFluidSingleSlotStorage implements SingleSlotStorage<FluidVariant> {
	private final ResourceHandler<FluidResource> inner;
	private final int slot;

	public NeoFluidSingleSlotStorage(ResourceHandler<FluidResource> inner, int slot) {
		this.inner = inner;
		this.slot = slot;
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		return Ints.saturatedCast(
				this.inner.insert(
						this.slot,
						TransferCompatUtil.toResource(resource),
						TransferCompatUtil.toNeoBucket(maxAmount),
						TransferCompatUtil.toNeoCtx(transaction)
				)
		);
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		return Ints.saturatedCast(
				this.inner.extract(
						this.slot,
						TransferCompatUtil.toResource(resource),
						TransferCompatUtil.toNeoBucket(maxAmount),
						TransferCompatUtil.toNeoCtx(transaction)
				)
		);
	}

	@Override
	public boolean isResourceBlank() {
		return this.inner.getResource(this.slot).isEmpty();
	}

	@Override
	public FluidVariant getResource() {
		return TransferCompatUtil.toVariant(this.inner.getResource(this.slot));
	}

	@Override
	public long getAmount() {
		return TransferCompatUtil.toFabricBucketLong(this.inner.getAmountAsLong(this.slot));
	}

	@Override
	public long getCapacity() {
		return TransferCompatUtil.toFabricBucketLong(this.inner.getCapacityAsLong(this.slot, this.inner.getResource(this.slot)));
	}
}
