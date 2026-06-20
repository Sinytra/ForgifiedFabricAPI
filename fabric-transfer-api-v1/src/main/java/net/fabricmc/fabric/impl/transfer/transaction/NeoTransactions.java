package net.fabricmc.fabric.impl.transfer.transaction;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.Lifecycle;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class NeoTransactions {
	public static Transaction openOuter() {
		return wrap(net.neoforged.neoforge.transfer.transaction.Transaction.openRoot());
	}

	public static Lifecycle getLifecycle() {
		return wrapLifecycle(net.neoforged.neoforge.transfer.transaction.Transaction.getLifecycle());
	}

	public static Transaction openNested(@Nullable TransactionContext maybeParent) {
		return wrap(net.neoforged.neoforge.transfer.transaction.Transaction.open(unwrapContext(maybeParent)));
	}

	public static Transaction openNested(@Nullable net.neoforged.neoforge.transfer.transaction.TransactionContext maybeParent) {
		return wrap(net.neoforged.neoforge.transfer.transaction.Transaction.open(maybeParent));
	}

	public static TransactionContext getCurrentUnsafe() {
		return wrap((net.neoforged.neoforge.transfer.transaction.Transaction) net.neoforged.neoforge.transfer.transaction.Transaction.getCurrentOpenedTransaction());
	}

	public static Transaction getOpenTransaction(int depth) {
		return wrap(TransactionManagerAccess.getOpenTransaction(depth));
	}

	public static Transaction wrap(net.neoforged.neoforge.transfer.transaction.Transaction inner) {
		return new NeoTransaction(inner);
	}

	@Nullable
	public static net.neoforged.neoforge.transfer.transaction.TransactionContext unwrapContext(@Nullable TransactionContext inner) {
		if (inner == null) {
			return null;
		}
		if (inner instanceof NeoTransaction tx) {
			return tx.getInner();
		}
		throw new UnsupportedOperationException();
	}

	public static Lifecycle wrapLifecycle(net.neoforged.neoforge.transfer.transaction.Transaction.Lifecycle neo) {
		return switch (neo) {
			case NONE -> Lifecycle.NONE;
			case OPEN -> Lifecycle.OPEN;
			case CLOSING -> Lifecycle.CLOSING;
			case ROOT_CLOSING -> Lifecycle.OUTER_CLOSING;
		};
	}
}
