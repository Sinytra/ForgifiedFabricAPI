package net.fabricmc.fabric.impl.transfer.compat;

import java.util.Collections;
import java.util.List;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.UnmodifiableView;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class NeoContainerItemContext implements ContainerItemContext {
	private final ItemAccess inner;

	public NeoContainerItemContext(ItemAccess inner) {
		this.inner = inner;
	}

	@Override
	public SingleSlotStorage<ItemVariant> getMainSlot() {
		return new ItemAccessSingleSlotStorage(this.inner);
	}

	@Override
	public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
		return insert(itemVariant, maxAmount, transactionContext);
	}

	@Override
	@UnmodifiableView
	public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
		if (this.inner instanceof FabricPlayerItemAccess ac) {
			return new FabricContainerStorage(ac.getInventoryWrapper()).getSlots();
		}
		return Collections.emptyList();
	}

	@Override
	public ItemVariant getItemVariant() {
		return TransferCompatUtil.toVariant(this.inner.getResource());
	}

	@Override
	public long getAmount() {
		return this.inner.getAmount();
	}

	@Override
	public long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
		return this.inner.insert(
				TransferCompatUtil.toResource(itemVariant),
				Ints.saturatedCast(maxAmount),
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}

	@Override
	public long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
		return this.inner.extract(
				TransferCompatUtil.toResource(itemVariant),
				Ints.saturatedCast(maxAmount),
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}

	@Override
	public long exchange(ItemVariant newVariant, long maxAmount, TransactionContext transaction) {
		return this.inner.exchange(
				TransferCompatUtil.toResource(newVariant),
				Ints.saturatedCast(maxAmount),
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}
}
