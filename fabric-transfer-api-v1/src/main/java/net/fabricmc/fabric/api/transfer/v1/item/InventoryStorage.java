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

package net.fabricmc.fabric.api.transfer.v1.item;

import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.impl.transfer.item.InventoryStorageImpl;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.Objects;

/**
 * An implementation of {@code Storage<ItemVariant>} for vanilla's {@link Container}, {@link net.minecraft.world.WorldlyContainer} and {@link net.minecraft.world.entity.player.Inventory}.
 *
 * <p>{@code Inventory} is often nicer to implement than {@code Storage<ItemVariant>}, but harder to use for item transfer.
 * This wrapper allows one to have the best of both worlds, for example by storing a subclass of {@link net.minecraft.world.WorldlyContainer} in a block entity class,
 * while exposing it as a {@code Storage<ItemVariant>} to {@linkplain ItemStorage#SIDED the item transfer API}.
 *
 * <p>In particular, note that {@link #getSlots} can be combined with {@link CombinedStorage} to retrieve a wrapper around a specific range of slots.
 *
 * <p><b>Important note:</b> This wrapper assumes that the inventory owns its slots.
 * If the inventory does not own its slots, for example because it delegates to another inventory, this wrapper should not be used!
 *
 * <p><b>Experimental feature</b>, we reserve the right to remove or change it without further notice.
 * The transfer API is a complex addition, and we want to be able to correct possible design mistakes.
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface InventoryStorage extends SlottedStorage<ItemVariant> {
	/**
	 * Return a wrapper around an {@link Container}.
	 *
	 * <p>If the inventory is a {@link net.minecraft.world.WorldlyContainer} and the direction is nonnull, the wrapper wraps the sided inventory from the given direction.
	 * The returned wrapper contains only the slots with the indices returned by {@link net.minecraft.world.WorldlyContainer#getSlotsForFace} at query time.
	 *
	 * @param inventory The inventory to wrap.
	 * @param direction The direction to use if the access is sided, or {@code null} if the access is not sided.
	 */
	static InventoryStorage of(Container inventory, @Nullable Direction direction) {
		Objects.requireNonNull(inventory, "Null inventory is not supported.");
		return InventoryStorageImpl.of(inventory, direction);
	}

	/**
	 * Retrieve an unmodifiable list of the wrappers for the slots in this inventory.
	 * Each wrapper corresponds to a single slot in the inventory.
	 */
	@Override
	@UnmodifiableView
	List<SingleSlotStorage<ItemVariant>> getSlots();

	@Override
	default int getSlotCount() {
		return getSlots().size();
	}

	@Override
	default SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return getSlots().get(slot);
	}
}
