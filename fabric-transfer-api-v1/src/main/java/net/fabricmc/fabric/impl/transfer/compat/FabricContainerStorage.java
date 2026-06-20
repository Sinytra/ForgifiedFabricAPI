package net.fabricmc.fabric.impl.transfer.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class FabricContainerStorage implements ContainerStorage {
	protected final ResourceHandler<ItemResource> inner;
	private final List<SingleSlotStorage<ItemVariant>> slots;

	public FabricContainerStorage(ResourceHandler<ItemResource> inner) {
		this.inner = inner;

		this.slots = new ArrayList<>();
		for (int i = 0; i < inner.size(); i++) {
			this.slots.add(new NeoItemSingleSlotStorage(inner, i));
		}
	}

	public static ContainerStorage of(Container container, @Nullable Direction direction) {
		if (container instanceof WorldlyContainer wc) {
			return new FabricContainerStorage(new WorldlyContainerWrapper(wc, direction));
		}
		return new FabricContainerStorage(VanillaContainerWrapper.of(container));
	}

	@UnmodifiableView
	@Override
	public List<SingleSlotStorage<ItemVariant>> getSlots() {
		return Collections.unmodifiableList(this.slots);
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
		return (Iterator) this.slots.iterator();
	}
}
