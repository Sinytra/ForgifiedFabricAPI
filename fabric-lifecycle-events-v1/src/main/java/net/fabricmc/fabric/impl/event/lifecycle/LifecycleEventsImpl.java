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

package net.fabricmc.fabric.impl.event.lifecycle;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;

public final class LifecycleEventsImpl implements ModInitializer {
	@Override
	public void onInitialize() {
		// Neo: fire chunk load/unload from NeoForge's chunk events instead of vanilla injections,
		// so they also fire when a mod replaces vanilla's chunk system and posts NeoForge's events itself.
		NeoForge.EVENT_BUS.addListener(ChunkEvent.Load.class, event -> {
			if (event.getLevel() instanceof ServerLevel level) {
				ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(level, event.getChunk(), event.isNewChunk());

				if (event.isNewChunk()) {
					ServerChunkEvents.CHUNK_GENERATE.invoker().onChunkGenerate(level, event.getChunk());
				}
			}
		});

		NeoForge.EVENT_BUS.addListener(ChunkEvent.Unload.class, event -> {
			if (event.getLevel() instanceof ServerLevel level) {
				ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(level, event.getChunk());
			}
		});

		// Part of impl for block entity events
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
			((LoadedChunksCache) level).fabric_markLoaded(chunk);
		});

		ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
			((LoadedChunksCache) level).fabric_markUnloaded(chunk);
		});

		// Fire block entity unload events.
		// This handles the edge case where going through a portal will cause block entities to unload without warning.
		ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
			for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
				ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, level);
			}
		});

		// We use the level unload event so levels that are dynamically hot(un)loaded get (block) entity unload events fired when shut down.
		ServerLevelEvents.UNLOAD.register((server, level) -> {
			for (LevelChunk chunk : ((LoadedChunksCache) level).fabric_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, level);
				}
			}

			for (Entity entity : level.getAllEntities()) {
				ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, level);
			}
		});
	}
}
