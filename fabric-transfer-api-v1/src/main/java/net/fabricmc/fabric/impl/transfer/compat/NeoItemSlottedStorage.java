package net.fabricmc.fabric.impl.transfer.compat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class NeoItemSlottedStorage implements SlottedStorage<ItemVariant> {
	private final ResourceHandler<ItemResource> inner;

	public NeoItemSlottedStorage(ResourceHandler<ItemResource> inner) {
		this.inner = inner;
	}

	@Override
	public int getSlotCount() {
		return this.inner.size();
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return new NeoItemSingleSlotStorage(this.inner, slot);
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
	public Iterator<StorageView<ItemVariant>> iterator() {
		List<StorageView<ItemVariant>> views = new ArrayList<>();
		for (int i = 0; i < this.inner.size(); i++) {
			views.add(new NeoItemSingleSlotStorage(this.inner, i));
		}
		return views.iterator();
	}
}
