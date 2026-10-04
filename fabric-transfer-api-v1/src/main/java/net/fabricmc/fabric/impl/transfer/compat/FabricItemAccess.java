package net.fabricmc.fabric.impl.transfer.compat;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;

public class FabricItemAccess implements ItemAccess {
	private final ContainerItemContext inner;

	public FabricItemAccess(ContainerItemContext inner) {
		this.inner = inner;
	}

	@Override
	public ItemResource getResource() {
		return TransferCompatUtil.toResource(this.inner.getItemVariant());
	}

	@Override
	public int getAmount() {
		return Ints.saturatedCast(this.inner.getAmount());
	}

	@Override
	public int insert(ItemResource resource, int amount, TransactionContext transaction) {
		return Ints.saturatedCast(
				this.inner.insert(
						TransferCompatUtil.toVariant(resource),
						amount,
						TransferCompatUtil.toFabricCtx(transaction)
				)
		);
	}

	@Override
	public int extract(ItemResource resource, int amount, TransactionContext transaction) {
		return Ints.saturatedCast(
				this.inner.extract(
						TransferCompatUtil.toVariant(resource),
						amount,
						TransferCompatUtil.toFabricCtx(transaction)
				)
		);
	}
}
