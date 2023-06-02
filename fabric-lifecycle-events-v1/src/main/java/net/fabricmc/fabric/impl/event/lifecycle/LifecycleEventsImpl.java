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

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.impl.client.event.lifecycle.ClientLifecycleEventsImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_lifecycle_events_v1")
public final class LifecycleEventsImpl {

    public LifecycleEventsImpl() {
        // Forgified FAPI: Use Forge events where possible
        MinecraftForge.EVENT_BUS.register(MinecraftServerEventHooks.class);
        MinecraftForge.EVENT_BUS.register(LivingEntityEventHooks.class);
        MinecraftForge.EVENT_BUS.register(ChunkEventHooks.class);
        MinecraftForge.EVENT_BUS.register(PlayerListHooks.class);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientLifecycleEventsImpl.onInitializeClient();
        }

        // Part of impl for block entity events
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            ((LoadedChunksCache) world).fabric_markLoaded(chunk);
        });

        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            ((LoadedChunksCache) world).fabric_markUnloaded(chunk);
        });

        // Fire block entity unload events.
        // This handles the edge case where going through a portal will cause block entities to unload without warning.
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, world);
            }
        });

        // We use the world unload event so worlds that are dynamically hot(un)loaded get (block) entity unload events fired when shut down.
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            for (LevelChunk chunk : ((LoadedChunksCache) world).fabric_getLoadedChunks()) {
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, world);
                }
            }

            for (Entity entity : world.getAllEntities()) {
                ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, world);
            }
        });
    }
}
