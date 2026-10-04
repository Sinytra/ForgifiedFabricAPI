package net.fabricmc.fabric.impl.transfer.compat;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.transaction.NeoTransactions;

public final class TransferCompatUtil {
	public static ItemResource toResource(ItemVariant variant) {
		return ItemResource.of(variant.typeHolder(), variant.getComponentsPatch());
	}

	public static ItemVariant toVariant(ItemResource inner) {
		return ItemVariant.of(inner.getItem(), inner.getComponentsPatch());
	}

	public static FluidResource toResource(FluidVariant variant) {
		return FluidResource.of(variant.typeHolder(), variant.getComponentsPatch());
	}

	public static FluidVariant toVariant(FluidResource inner) {
		return FluidVariant.of(inner.getFluid(), inner.getComponentsPatch());
	}

	public static TransactionContext toFabricCtx(net.neoforged.neoforge.transfer.transaction.TransactionContext inner) {
		return NeoTransactions.wrap((net.neoforged.neoforge.transfer.transaction.Transaction) inner);
	}

	public static net.neoforged.neoforge.transfer.transaction.TransactionContext toNeoCtx(TransactionContext inner) {
		return NeoTransactions.unwrapContext(inner);
	}

	public static int toNeoBucket(long amount) {
		return (int) toNeoBucketLong(amount);
	}

	public static long toNeoBucketLong(long amount) {
		return (long) (Ints.saturatedCast(amount) / (double) FluidConstants.BUCKET * FluidType.BUCKET_VOLUME);
	}

	public static int toFabricBucket(long amount) {
		return (int) toFabricBucketLong(amount);
	}

	public static long toFabricBucketLong(long amount) {
		return (long) (amount / (double) FluidType.BUCKET_VOLUME * FluidConstants.BUCKET);
	}
}
