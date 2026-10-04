package net.fabricmc.fabric.impl.transfer.compat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class NeoFluidSlottedStorage implements SlottedStorage<FluidVariant> {
	private final ResourceHandler<FluidResource> inner;

	public NeoFluidSlottedStorage(ResourceHandler<FluidResource> inner) {
		this.inner = inner;
	}

	@Override
	public int getSlotCount() {
		return this.inner.size();
	}

	@Override
	public SingleSlotStorage<FluidVariant> getSlot(int slot) {
		return new NeoFluidSingleSlotStorage(this.inner, slot);
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		return this.inner.insert(
				TransferCompatUtil.toResource(resource),
				TransferCompatUtil.toNeoBucket(maxAmount),
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		return this.inner.extract(
				TransferCompatUtil.toResource(resource),
				TransferCompatUtil.toNeoBucket(maxAmount),
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}

	@Override
	public Iterator<StorageView<FluidVariant>> iterator() {
		List<StorageView<FluidVariant>> views = new ArrayList<>();
		for (int i = 0; i < this.inner.size(); i++) {
			views.add(new NeoFluidSingleSlotStorage(this.inner, i));
		}
		return views.iterator();
	}
}
