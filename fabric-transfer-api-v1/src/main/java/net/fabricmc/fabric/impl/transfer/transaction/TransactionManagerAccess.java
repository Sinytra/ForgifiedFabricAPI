package net.fabricmc.fabric.impl.transfer.transaction;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import net.neoforged.neoforge.transfer.transaction.Transaction;

import net.fabricmc.fabric.impl.transfer.compat.FabricTransactionManager;

public class TransactionManagerAccess {
	private static final Class<?> TX_MNG_CLASS;
	private static final MethodHandle GET_MNG_FOR_THREAD;
	private static final MethodHandle GET_OPEN_TX;

	static {
		try {
			TX_MNG_CLASS = Class.forName("net.neoforged.neoforge.transfer.transaction.TransactionManager");
			Lookup lookup = MethodHandles.privateLookupIn(TX_MNG_CLASS, MethodHandles.lookup());
			GET_MNG_FOR_THREAD = lookup.findStatic(TX_MNG_CLASS, "getManagerForThread", MethodType.methodType(TX_MNG_CLASS));
			GET_OPEN_TX = lookup.findVirtual(TX_MNG_CLASS, "getOpenTransaction", MethodType.methodType(Transaction.class, int.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Transaction getOpenTransaction(int depth) {
		try {
			Object manager = GET_MNG_FOR_THREAD.invoke();
			return (Transaction) GET_OPEN_TX.invoke(manager, depth);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static FabricTransactionManager getManager() {
		try {
			Object manager = GET_MNG_FOR_THREAD.invoke();
			return (FabricTransactionManager) manager;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
