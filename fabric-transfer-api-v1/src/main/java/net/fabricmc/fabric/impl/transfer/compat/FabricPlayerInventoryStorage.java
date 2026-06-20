package net.fabricmc.fabric.impl.transfer.compat;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class FabricPlayerInventoryStorage extends FabricContainerStorage implements PlayerInventoryStorage {

	public FabricPlayerInventoryStorage(ResourceHandler<ItemResource> inner) {
		super(inner);
	}

	public static PlayerInventoryStorage of(Inventory inventory) {
		return new FabricPlayerInventoryStorage(PlayerInventoryWrapper.of(inventory));
	}

	@Override
	public long offer(ItemVariant resource, long amount, TransactionContext tx) {
		return this.insert(resource, amount, tx);
	}

	@Override
	public void drop(ItemVariant variant, long amount, boolean throwRandomly, boolean retainOwnership, TransactionContext transaction) {
		((PlayerInventoryWrapper) this.inner).drop(
				TransferCompatUtil.toResource(variant),
				Ints.checkedCast(amount),
				throwRandomly,
				retainOwnership,
				TransferCompatUtil.toNeoCtx(transaction)
		);
	}

	@Override
	public SingleSlotStorage<ItemVariant> getHandSlot(InteractionHand hand) {
		return new NeoItemSingleSlotStorage(((PlayerInventoryWrapper) this.inner).getHandSlot(hand), 0);
	}
}
