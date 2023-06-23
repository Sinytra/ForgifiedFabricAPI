package net.fabricmc.fabric.impl.transfer.compat;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

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
        return ForgeCompatUtil.toForgeFluidStack(slots.get(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        StorageView<FluidVariant> view = slots.get(tank);
        return view != null ? (int) view.getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return storage.simulateInsert(ForgeCompatUtil.toFluidStorageView(stack), ForgeCompatUtil.toFabricBucket(stack.getAmount()), null) > 0;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        try (Transaction transaction = Transaction.openOuter()) {
            FluidVariant variant = ForgeCompatUtil.toFluidStorageView(resource);
            int filled = (int) storage.insert(variant, ForgeCompatUtil.toFabricBucket(resource.getAmount()), transaction);
            if (action.execute()) {
                transaction.commit();
            }
            return ForgeCompatUtil.toForgeBucket(filled);
        }
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (!resource.isEmpty()) {
            try (Transaction transaction = Transaction.openOuter()) {
                FluidVariant variant = ForgeCompatUtil.toFluidStorageView(resource);
                int drained = (int) storage.extract(variant, ForgeCompatUtil.toFabricBucket(resource.getAmount()), transaction);
                if (action.execute()) {
                    transaction.commit();
                }
                return ForgeCompatUtil.toForgeFluidStack(variant, drained);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
            try (Transaction transaction = Transaction.openOuter()) {
                FluidVariant resource = view.getResource();
                int drained = (int) storage.extract(resource, maxDrain, transaction);
                if (action.execute()) {
                    transaction.commit();
                }
                return ForgeCompatUtil.toForgeFluidStack(resource, drained);
            }
        }
        return FluidStack.EMPTY;
    }
}
