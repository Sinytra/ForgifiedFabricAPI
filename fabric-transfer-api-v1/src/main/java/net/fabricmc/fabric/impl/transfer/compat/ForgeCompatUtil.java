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
import net.minecraftforge.fluids.FluidType;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public final class ForgeCompatUtil {

    public static int toForgeBucket(int amount) {
        return (int) (amount / (double) FluidConstants.BUCKET * FluidType.BUCKET_VOLUME);
    }

    public static int toFabricBucket(int amount) {
        return (int) (amount / (double) FluidType.BUCKET_VOLUME * FluidConstants.BUCKET);
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

    private ForgeCompatUtil() {}
}
