package net.fabricmc.fabric.mixin.transfer;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.Result;
import net.fabricmc.fabric.impl.transfer.compat.TransferApiNeoCompat;

import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.OuterCloseCallback;
import net.fabricmc.fabric.impl.transfer.compat.FabricTransactionManager;

@Mixin(targets = "net.neoforged.neoforge.transfer.transaction.TransactionManager")
public class TransactionManagerMixin implements FabricTransactionManager {
	@Unique
	private final List<OuterCloseCallback> fabric$outerCloseCallbacks = new ArrayList<>();

	@Override
	public void addOuterCloseCallback(OuterCloseCallback outerCloseCallback) {
		this.fabric$outerCloseCallbacks.add(outerCloseCallback);
	}

	@ModifyVariable(
			method = "processRootCommitQueue",
			at = @At(
					value = "FIELD",
					target = "processingRootCommitQueue:Z",
					opcode = Opcodes.PUTFIELD,
					shift = Shift.AFTER
			)
	)
	private RuntimeException processOuterCallbacks(@Nullable RuntimeException closeException) {
		Boolean wasAborted = TransferApiNeoCompat.WAS_ABORTED.get();
		Result result = wasAborted == null || !wasAborted ? Result.COMMITTED : Result.ABORTED;
		
		// Invoke outer close callbacks in reverse order
		for (int i = fabric$outerCloseCallbacks.size() - 1; i >= 0; i--) {
			try {
				fabric$outerCloseCallbacks.get(i).afterOuterClose(result);
			} catch (Exception exception) {
				if (closeException == null) {
					closeException = new RuntimeException("Encountered an exception while invoking a transaction outer close callback.", exception);
				} else {
					closeException.addSuppressed(exception);
				}
			}
		}

		fabric$outerCloseCallbacks.clear();
		
		return closeException;
	}
}
