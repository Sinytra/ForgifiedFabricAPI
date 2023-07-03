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

package net.fabricmc.fabric.test.transfer.ingame;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleItemStorage;

public class ItemChuteBlockEntity extends BlockEntity {
	final SingleItemStorage storage = new SingleItemStorage() {
		@Override
		protected long getCapacity(ItemVariant variant) {
			return 1;
		}

		@Override
		protected void onFinalCommit() {
			super.onFinalCommit();
			markDirty();
		}
	};

	public ItemChuteBlockEntity(BlockPos pos, BlockState state) {
		super(TransferTestInitializer.ITEM_CHUTE_TYPE.get(), pos, state);
	}

	@SuppressWarnings("ConstantConditions")
	public void tick() {
		
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		storage.writeNbt(nbt);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		storage.readNbt(nbt);
	}
}
