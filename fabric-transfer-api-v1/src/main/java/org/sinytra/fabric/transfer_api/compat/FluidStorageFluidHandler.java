package org.sinytra.fabric.transfer_api.compat;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class FluidStorageFluidHandler implements IFluidHandler {
    private final Storage<FluidVariant> storage;
    private final Int2ObjectMap<StorageView<FluidVariant>> slots;

    public FluidStorageFluidHandler(Storage<FluidVariant> storage) {
        this.storage = storage;
        this.slots = new Int2ObjectOpenHashMap<>();
        int i = 0;
        for (StorageView<FluidVariant> view : storage) {
            slots.put(i++, view);
        }
    }

    @Override
    public int getTanks() {
        return slots.size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return NeoCompatUtil.toForgeFluidStack(slots.get(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        StorageView<FluidVariant> view = slots.get(tank);
        return view != null ? (int) view.getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return StorageUtil.simulateInsert(storage, NeoCompatUtil.toFluidStorageView(stack), NeoCompatUtil.toFabricBucket(stack.getAmount()), null) > 0;
    }
    
    // if a transaction from a fabric context causes a neoforge mod to create a new transaction to a fabric context as part of that first call we need to do so in a new thread
    private <T> T executeWithTransactionHandling(Supplier<T> operation, T defaultValue) {
        if (Transaction.isOpen()) {
            try {
                return CompletableFuture.supplyAsync(operation).get(1, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                return defaultValue;
            }
        } else {
            return operation.get();
        }
    }

    @Override
    public int fill(FluidStack resource, @NotNull FluidAction action) {
        
        // because moving blank/empty fluid resources is a thing in neoforge for some reason? (See https://github.com/AztechMC/Modern-Industrialization/issues/1029)
        if (resource.isEmpty()) return 0;
        
        return executeWithTransactionHandling(() -> {
            try (Transaction transaction = Transaction.openOuter()) {
                FluidVariant variant = NeoCompatUtil.toFluidStorageView(resource);
                int filled = (int) storage.insert(variant, NeoCompatUtil.toFabricBucket(resource.getAmount()), transaction);
                if (action.execute()) {
                    transaction.commit();
                }
                return NeoCompatUtil.toForgeBucket(filled);
            }
        }, 0);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, @NotNull FluidAction action) {
        
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        
        return executeWithTransactionHandling(() -> {
            try (Transaction transaction = Transaction.openOuter()) {
                FluidVariant variant = NeoCompatUtil.toFluidStorageView(resource);
                int drained = (int) storage.extract(variant, NeoCompatUtil.toFabricBucket(resource.getAmount()), transaction);
                if (action.execute()) {
                    transaction.commit();
                }
                return NeoCompatUtil.toForgeFluidStack(variant, drained);
            }
        }, FluidStack.EMPTY);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
        return executeWithTransactionHandling(() -> {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    FluidVariant resource = view.getResource();
                    int drained = (int) storage.extract(resource, NeoCompatUtil.toFabricBucket(maxDrain), transaction);
                    if (action.execute()) {
                        transaction.commit();
                    }
                    return NeoCompatUtil.toForgeFluidStack(resource, drained);
                }
            }
            return FluidStack.EMPTY;
        }, FluidStack.EMPTY);
    }
}
