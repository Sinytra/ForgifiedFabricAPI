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

package net.fabricmc.fabric.test.screenhandler.screen;

import net.fabricmc.fabric.test.screenhandler.ScreenHandlerTest;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

public class PositionedBagScreenHandler extends BagScreenHandler implements PositionedScreenHandler {
	private final BlockPos pos;

	public PositionedBagScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(syncId, playerInventory, new SimpleContainer(9), readOptionalPos(buf));
	}

	private static BlockPos readOptionalPos(FriendlyByteBuf buf) {
		boolean hasPos = buf.readBoolean();
		BlockPos pos = buf.readBlockPos();
		return hasPos ? pos : null;
	}

	public PositionedBagScreenHandler(int syncId, Inventory playerInventory, Container inventory, BlockPos pos) {
		super(ScreenHandlerTest.POSITIONED_BAG_SCREEN_HANDLER.get(), syncId, playerInventory, inventory);
		this.pos = pos;
	}

	@Override
	public BlockPos getPos() {
		return pos;
	}
}
