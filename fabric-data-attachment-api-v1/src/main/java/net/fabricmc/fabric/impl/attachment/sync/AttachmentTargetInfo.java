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

package net.fabricmc.fabric.impl.attachment.sync;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;

public sealed interface AttachmentTargetInfo<T> {
	int MAX_SIZE_IN_BYTES = Byte.BYTES + Long.BYTES;
	StreamCodec<ByteBuf, AttachmentTargetInfo<?>> PACKET_CODEC = ByteBufCodecs.BYTE.dispatch(
			AttachmentTargetInfo::getId, Type::packetCodecFromId
	);

	Type<T> getType();

	default byte getId() {
		return getType().id;
	}

	AttachmentTarget getTarget(Level world);

	record Type<T>(byte id, StreamCodec<ByteBuf, ? extends AttachmentTargetInfo<T>> packetCodec) {
		static Byte2ObjectMap<Type<?>> TYPES = new Byte2ObjectArrayMap<>();
		static Type<BlockEntity> BLOCK_ENTITY = new Type<>((byte) 0, BlockEntityTarget.PACKET_CODEC);
		static Type<Entity> ENTITY = new Type<>((byte) 1, EntityTarget.PACKET_CODEC);
		static Type<ChunkAccess> CHUNK = new Type<>((byte) 2, ChunkTarget.PACKET_CODEC);
		static Type<Level> WORLD = new Type<>((byte) 3, WorldTarget.PACKET_CODEC);

		public Type {
			TYPES.put(id, this);
		}

		static StreamCodec<ByteBuf, ? extends AttachmentTargetInfo<?>> packetCodecFromId(byte id) {
			return TYPES.get(id).packetCodec;
		}
	}

	record BlockEntityTarget(BlockPos pos) implements AttachmentTargetInfo<BlockEntity> {
		static final StreamCodec<ByteBuf, BlockEntityTarget> PACKET_CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, BlockEntityTarget::pos,
				BlockEntityTarget::new
		);

		@Override
		public Type<BlockEntity> getType() {
			return Type.BLOCK_ENTITY;
		}

		@Override
		public AttachmentTarget getTarget(Level world) {
			return world.getBlockEntity(pos);
		}
	}

	record EntityTarget(int networkId) implements AttachmentTargetInfo<Entity> {
		static final StreamCodec<ByteBuf, EntityTarget> PACKET_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, EntityTarget::networkId,
				EntityTarget::new
		);

		@Override
		public Type<Entity> getType() {
			return Type.ENTITY;
		}

		@Override
		public AttachmentTarget getTarget(Level world) {
			return world.getEntity(networkId);
		}
	}

	record ChunkTarget(ChunkPos pos) implements AttachmentTargetInfo<ChunkAccess> {
		static final StreamCodec<ByteBuf, ChunkTarget> PACKET_CODEC = ByteBufCodecs.VAR_LONG
				.map(ChunkPos::new, ChunkPos::toLong)
				.map(ChunkTarget::new, ChunkTarget::pos);

		@Override
		public Type<ChunkAccess> getType() {
			return Type.CHUNK;
		}

		@Override
		public AttachmentTarget getTarget(Level world) {
			return world.getChunk(pos.x, pos.z);
		}
	}

	final class WorldTarget implements AttachmentTargetInfo<Level> {
		public static final WorldTarget INSTANCE = new WorldTarget();
		static final StreamCodec<ByteBuf, WorldTarget> PACKET_CODEC = StreamCodec.unit(INSTANCE);

		private WorldTarget() {
		}

		@Override
		public Type<Level> getType() {
			return Type.WORLD;
		}

		@Override
		public AttachmentTarget getTarget(Level world) {
			return world;
		}
	}
}
