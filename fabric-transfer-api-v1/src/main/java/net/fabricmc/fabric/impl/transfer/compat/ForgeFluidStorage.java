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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ForgeFluidStorage implements Storage<FluidVariant> {
    private final IFluidHandler handler;

    public ForgeFluidStorage(IFluidHandler handler) {
        this.handler = handler;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        FluidStack stack = ForgeCompatUtil.toForgeFluidStack(resource, (int) maxAmount);
        int filled = handler.fill(stack, IFluidHandler.FluidAction.SIMULATE);
        transaction.addCloseCallback((context, result) -> {
            if (result.wasCommitted()) {
                handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            }
        });
        return ForgeCompatUtil.toFabricBucket(filled);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        FluidStack stack = ForgeCompatUtil.toForgeFluidStack(resource, (int) maxAmount);
        FluidStack drained = handler.drain(stack, IFluidHandler.FluidAction.SIMULATE);
        transaction.addCloseCallback((context, result) -> {
            if (result.wasCommitted()) {
                handler.drain(stack, IFluidHandler.FluidAction.EXECUTE);
            }
        });
        return ForgeCompatUtil.toFabricBucket(drained.getAmount());
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        List<StorageView<FluidVariant>> views = new ArrayList<>();
        for (int i = 0; i < handler.getTanks(); i++) {
            views.add(new ForgeFluidView(handler, i));
        }
        return views.iterator();
    }
}
