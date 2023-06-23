package net.fabricmc.fabric.impl.transfer.compat;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

public final class ForgeCompatUtil {

    public static int toForgeBucket(int amount) {
        return (int) (amount / FluidConstants.BUCKET * FluidType.BUCKET_VOLUME);
    }

    public static int toFabricBucket(int amount) {
        return (int) (amount / FluidType.BUCKET_VOLUME * FluidConstants.BUCKET);
    }

    public static FluidStack toForgeFluidStack(StorageView<FluidVariant> view) {
        if (view != null && !view.isResourceBlank()) {
            FluidVariant resource = view.getResource();
            return new FluidStack(resource.getFluid(), toForgeBucket((int) view.getAmount()), resource.getNbt());
        }
        return FluidStack.EMPTY;
    }

    public static FluidStack toForgeFluidStack(FluidVariant variant, int amount) {
        return !variant.isBlank() && amount > 0 ? new FluidStack(variant.getFluid(), ForgeCompatUtil.toForgeBucket(amount), variant.getNbt()) : FluidStack.EMPTY;
    }

    public static FluidVariant toFluidStorageView(FluidStack stack) {
        return !stack.isEmpty() ? FluidVariant.of(stack.getFluid(), stack.getTag()) : FluidVariant.blank();
    }

    private ForgeCompatUtil() {
    }
}
