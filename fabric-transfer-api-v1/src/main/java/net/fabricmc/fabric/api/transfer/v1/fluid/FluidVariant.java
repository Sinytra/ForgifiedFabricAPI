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

package net.fabricmc.fabric.api.transfer.v1.fluid;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable association of a still fluid and an optional NBT tag.
 *
 * <p>Do not extend this class. Use {@link #of(Fluid)} and {@link #of(Fluid, CompoundTag)} to create instances.
 *
 * <p>{@link net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering} can be used for client-side rendering of fluid variants.
 *
 * <p><b>Fluid variants must always be compared with {@code equals}, never by reference!</b>
 * {@code hashCode} is guaranteed to be correct and constant time independently of the size of the NBT.
 *
 * <p><b>Experimental feature</b>, we reserve the right to remove or change it without further notice.
 * The transfer API is a complex addition, and we want to be able to correct possible design mistakes.
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface FluidVariant extends TransferVariant<Fluid> {
	/**
	 * Retrieve a blank FluidVariant.
	 */
	static FluidVariant blank() {
		return of(Fluids.EMPTY);
	}

	/**
	 * Retrieve a FluidVariant with a fluid, and a {@code null} tag.
	 *
	 * <p>The flowing and still variations of {@linkplain net.minecraft.world.level.material.FlowingFluid flowable fluids}
	 * are normalized to always refer to the still variant. For example,
	 * {@code FluidVariant.of(Fluids.FLOWING_WATER).getFluid() == Fluids.WATER}.
	 */
	static FluidVariant of(Fluid fluid) {
		return of(fluid, null);
	}

	/**
	 * Retrieve a FluidVariant with a fluid, and an optional tag.
	 *
	 * <p>The flowing and still variations of {@linkplain net.minecraft.world.level.material.FlowingFluid flowable fluids}
	 * are normalized to always refer to the still fluid. For example,
	 * {@code FluidVariant.of(Fluids.FLOWING_WATER, nbt).getFluid() == Fluids.WATER}.
	 */
	static FluidVariant of(Fluid fluid, @Nullable CompoundTag nbt) {
		return FluidVariantImpl.of(fluid, nbt);
	}

	/**
	 * Return the fluid of this variant.
	 */
	default Fluid getFluid() {
		return getObject();
	}

	/**
	 * Deserialize a variant from an NBT compound tag, assuming it was serialized using {@link #toNbt}.
	 *
	 * <p>If an error occurs during deserialization, it will be logged with the DEBUG level, and a blank variant will be returned.
	 */
	static FluidVariant fromNbt(CompoundTag nbt) {
		return FluidVariantImpl.fromNbt(nbt);
	}

	/**
	 * Read a variant from a packet byte buffer, assuming it was serialized using {@link #toPacket}.
	 */
	static FluidVariant fromPacket(FriendlyByteBuf buf) {
		return FluidVariantImpl.fromPacket(buf);
	}
}
