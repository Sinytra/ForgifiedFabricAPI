package net.fabricmc.fabric.impl.transfer.compat;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.OuterCloseCallback;

public interface FabricTransactionManager {
	void addOuterCloseCallback(OuterCloseCallback outerCloseCallback);
}
