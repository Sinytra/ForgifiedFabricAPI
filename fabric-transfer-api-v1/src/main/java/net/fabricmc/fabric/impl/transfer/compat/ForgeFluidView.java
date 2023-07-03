package net.fabricmc.fabric.impl.transfer.compat;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public record ForgeFluidView(IFluidHandler handler, int tank) implements StorageView<FluidVariant> {
    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        FluidStack stack = getStack();
        if (!stack.isEmpty() && resource.isOf(stack.getFluid()) && resource.nbtMatches(stack.getTag())) {
            FluidStack existing = new FluidStack(stack.getFluid(), ForgeCompatUtil.toForgeBucket((int) maxAmount), stack.getTag());
            FluidStack drained = handler.drain(existing, IFluidHandler.FluidAction.SIMULATE);
            transaction.addCloseCallback((context, result) -> {
                if (result.wasCommitted()) {
                    handler.drain(existing, IFluidHandler.FluidAction.EXECUTE);
                }
            });
            return ForgeCompatUtil.toFabricBucket(drained.getAmount());
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return handler.getFluidInTank(tank).isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        return ForgeCompatUtil.toFluidStorageView(getStack());
    }

    @Override
    public long getAmount() {
        return ForgeCompatUtil.toFabricBucket(getStack().getAmount());
    }

    @Override
    public long getCapacity() {
        return ForgeCompatUtil.toFabricBucket(handler.getTankCapacity(tank));
    }

    private FluidStack getStack() {
        return handler.getFluidInTank(tank);
    }
}
