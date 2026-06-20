package net.fabricmc.fabric.impl.transfer.transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.compat.FabricTransaction;

public class NeoTransaction implements Transaction {
	private final net.neoforged.neoforge.transfer.transaction.Transaction inner;

	public NeoTransaction(net.neoforged.neoforge.transfer.transaction.Transaction inner) {
		this.inner = inner;
	}

	public net.neoforged.neoforge.transfer.transaction.Transaction getInner() {
		return inner;
	}

	@Override
	public void abort() {
		this.inner.close();
	}

	@Override
	public void commit() {
		this.inner.commit();
	}

	@Override
	public void close() {
		this.inner.close();
	}

	@Override
	public Transaction openNested() {
		return NeoTransactions.openNested(this.inner);
	}

	@Override
	public int nestingDepth() {
		return this.inner.depth();
	}

	@Override
	public Transaction getOpenTransaction(int nestingDepth) {
		return NeoTransactions.getOpenTransaction(nestingDepth);
	}

	@Override
	public void addCloseCallback(CloseCallback closeCallback) {
		((FabricTransaction) (Object) this.inner).addCloseCallback(closeCallback);
	}

	@Override
	public void addOuterCloseCallback(OuterCloseCallback outerCloseCallback) {
		TransactionManagerAccess.getManager().addOuterCloseCallback(outerCloseCallback);
	}
}
