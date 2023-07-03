package net.fabricmc.fabric.impl.transfer.compat;

import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public class FluidStorageFluidHandlerItem extends FluidStorageFluidHandler implements IFluidHandlerItem {
    private final ItemStack container;

    public FluidStorageFluidHandlerItem(Storage<FluidVariant> storage, ItemStack container) {
        super(storage);

        this.container = container;
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    }
}
