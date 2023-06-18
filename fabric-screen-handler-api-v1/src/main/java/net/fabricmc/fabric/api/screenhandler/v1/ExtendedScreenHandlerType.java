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

package net.fabricmc.fabric.api.screenhandler.v1;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.Objects;

/**
 * A {@link MenuType} for an extended screen handler that
 * synchronizes additional data to the client when it is opened.
 *
 * <p>Extended screen handlers can be opened using
 * {@link net.minecraft.world.entity.player.Player#openMenu(MenuProvider)
 * PlayerEntity.openHandledScreen} with an
 * {@link ExtendedScreenHandlerFactory}.
 *
 * <h2>Example</h2>
 * <pre>
 * {@code
 * // Creating and registering the type
 * public static final ExtendedScreenHandlerType<OvenScreenHandler> OVEN =
 * 	new ExtendedScreenHandlerType((syncId, inventory, buf) -> ...);
 * Registry.register(Registry.SCREEN_HANDLER, new Identifier(...), OVEN);
 *
 * // Note: remember to also register the screen using vanilla's HandledScreens!
 *
 * // Screen handler class
 * public class OvenScreenHandler extends ScreenHandler {
 * 	public OvenScreenHandler(int syncId) {
 * 		super(MyScreenHandlers.OVEN, syncId);
 * 	}
 * }
 *
 * // Opening the extended screen handler
 * var factory = new ExtendedScreenHandlerFactory() {
 * 	...
 * };
 * player.openHandlerScreen(factory); // only works on ServerPlayerEntity instances
 * }
 * </pre>
 *
 * @param <T> the type of screen handler created by this type
 */
public class ExtendedScreenHandlerType<T extends AbstractContainerMenu> extends MenuType<T> {
	private final ExtendedFactory<T> factory;

	/**
	 * Constructs an extended screen handler type.
	 *
	 * @param factory the screen handler factory used for {@link #create(int, Inventory, FriendlyByteBuf)}
	 */
	public ExtendedScreenHandlerType(ExtendedFactory<T> factory) {
		super(null, FeatureFlags.VANILLA_SET);
		this.factory = Objects.requireNonNull(factory, "screen handler factory cannot be null");
	}

	/**
	 * @throws UnsupportedOperationException always; use {@link #create(int, Inventory, FriendlyByteBuf)}
	 * @deprecated Use {@link #create(int, Inventory, FriendlyByteBuf)} instead.
	 */
	@Deprecated
	@Override
	public final T create(int syncId, Inventory inventory) {
		throw new UnsupportedOperationException("Use ExtendedScreenHandlerType.create(int, PlayerInventory, PacketByteBuf)!");
	}

	/**
	 * Creates a new screen handler using the extra opening data.
	 *
	 * @param syncId    the sync ID
	 * @param inventory the player inventory
	 * @param buf       the buffer containing the synced opening data
	 * @return the created screen handler
	 */
	public T create(int syncId, Inventory inventory, FriendlyByteBuf buf) {
		return factory.create(syncId, inventory, buf);
	}

	/**
	 * A factory for creating screen handler instances from
	 * additional opening data.
	 * This is primarily used on the client, but can be called on the
	 * server too.
	 *
	 * @param <T> the type of screen handlers created
	 * @see #create(int, Inventory, FriendlyByteBuf)
	 */
	@FunctionalInterface
	public interface ExtendedFactory<T extends AbstractContainerMenu> {
		/**
		 * Creates a new screen handler with additional screen opening data.
		 *
		 * @param syncId    the synchronization ID
		 * @param inventory the player inventory
		 * @param buf       the packet buffer
		 * @return the created screen handler
		 */
		T create(int syncId, Inventory inventory, FriendlyByteBuf buf);
	}
}
