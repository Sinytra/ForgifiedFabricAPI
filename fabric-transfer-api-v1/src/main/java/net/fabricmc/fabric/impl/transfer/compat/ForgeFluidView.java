/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
