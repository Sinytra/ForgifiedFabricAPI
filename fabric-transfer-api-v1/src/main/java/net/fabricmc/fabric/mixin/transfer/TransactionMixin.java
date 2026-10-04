package net.fabricmc.fabric.mixin.transfer;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.impl.transfer.compat.TransferApiNeoCompat;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.CloseCallback;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.Result;
import net.fabricmc.fabric.impl.transfer.compat.FabricTransaction;
import net.fabricmc.fabric.impl.transfer.transaction.NeoTransactions;

@Mixin(Transaction.class)
public class TransactionMixin implements FabricTransaction {
	@Unique
	private final List<CloseCallback> fabric$closeCallbacks = new ArrayList<>();

	@Override
	public void addCloseCallback(CloseCallback closeCallback) {
		this.fabric$closeCallbacks.add(closeCallback);
	}

	@ModifyVariable(method = "close(Z)V", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
	private RuntimeException processCloseCallbacks(RuntimeException closeException, boolean wasAborted) {
		Result result = wasAborted ? Result.ABORTED : Result.COMMITTED;
		Transaction tx = (Transaction) (Object) this;
		net.fabricmc.fabric.api.transfer.v1.transaction.Transaction fabricTx = NeoTransactions.wrap(tx);

		// Invoke callbacks in reverse order
		for (int i = fabric$closeCallbacks.size() - 1; i >= 0; i--) {
			try {
				fabric$closeCallbacks.get(i).onClose(fabricTx, result);
			} catch (Exception exception) {
				if (closeException == null) {
					closeException = new RuntimeException("Encountered an exception while invoking a transaction close callback.", exception);
				} else {
					closeException.addSuppressed(exception);
				}
			}
		}

		fabric$closeCallbacks.clear();

		return closeException;
	}

	@Inject(method = "close(Z)V", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/transfer/transaction/TransactionManager;processRootCommitQueue(Ljava/lang/RuntimeException;)Ljava/lang/RuntimeException;"))
	private void setWasAborted(boolean wasAborted, CallbackInfo ci) {
		TransferApiNeoCompat.WAS_ABORTED.set(wasAborted);
	}

	@Inject(method = "close(Z)V", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/transfer/transaction/TransactionManager;processRootCommitQueue(Ljava/lang/RuntimeException;)Ljava/lang/RuntimeException;", shift = Shift.AFTER))
	private void resetWasAborted(boolean wasAborted, CallbackInfo ci) {
		TransferApiNeoCompat.WAS_ABORTED.remove();
	}
}
