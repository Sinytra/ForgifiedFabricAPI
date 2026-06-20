package net.fabricmc.fabric.impl.transfer.compat;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class FabricItemResourceHandler implements ResourceHandler<ItemResource> {
	private final Storage<ItemVariant> inner;
	private final Int2ObjectMap<StorageView<ItemVariant>> slots;

	public FabricItemResourceHandler(Storage<ItemVariant> inner) {
		this.inner = inner;

		this.slots = new Int2ObjectOpenHashMap<>();
		int i = 0;
		for (StorageView<ItemVariant> view : inner) {
			slots.put(i++, view);
		}
	}

	@Override
	public int size() {
		return Math.max(slots.size(), 1);
	}

	@Override
	public ItemResource getResource(int index) {
		if (index >= slots.size()) return ItemResource.EMPTY;
		return TransferCompatUtil.toResource(slots.get(index).getResource());
	}

	@Override
	public long getAmountAsLong(int index) {
		if (index >= slots.size()) return 0;
		return slots.get(index).getAmount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		if (index >= slots.size()) return 0;
		return slots.get(index).getCapacity();
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return StorageUtil.simulateInsert(
				this.inner,
				TransferCompatUtil.toVariant(resource),
				resource.getMaxStackSize(),
				null
		) > 0;
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		return Ints.saturatedCast(
				this.inner.insert(
						TransferCompatUtil.toVariant(resource),
						amount,
						TransferCompatUtil.toFabricCtx(transaction)
				)
		);
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		return Ints.saturatedCast(
				this.inner.extract(
						TransferCompatUtil.toVariant(resource),
						amount,
						TransferCompatUtil.toFabricCtx(transaction)
				)
		);
	}
}
