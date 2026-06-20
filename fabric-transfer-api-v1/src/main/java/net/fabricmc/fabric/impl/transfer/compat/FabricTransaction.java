package net.fabricmc.fabric.impl.transfer.compat;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.CloseCallback;

public interface FabricTransaction {
	void addCloseCallback(CloseCallback closeCallback);
}
