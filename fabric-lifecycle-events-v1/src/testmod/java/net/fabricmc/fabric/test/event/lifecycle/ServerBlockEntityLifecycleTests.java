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

package net.fabricmc.fabric.test.event.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public final class ServerBlockEntityLifecycleTests {
    private static final boolean PRINT_SERVER_BLOCKENTITY_MESSAGES = System.getProperty("fabric-lifecycle-events-testmod.printServerBlockEntityMessages") != null;
    private static final List<BlockEntity> serverBlockEntities = new ArrayList<>();

    public static void onInitialize() {
        final Logger logger = ServerLifecycleTests.LOGGER;

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> {
            serverBlockEntities.add(blockEntity);

            if (PRINT_SERVER_BLOCKENTITY_MESSAGES) {
                logger.info("[SERVER] LOADED " + ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType()).toString() + " - BlockEntities: " + serverBlockEntities.size());
            }
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
            serverBlockEntities.remove(blockEntity);

            if (PRINT_SERVER_BLOCKENTITY_MESSAGES) {
                logger.info("[SERVER] UNLOADED " + ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType()).toString() + " - BlockEntities: " + serverBlockEntities.size());
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {
            if (minecraftServer.getTickCount() % 200 == 0) {
                int entities = 0;

                if (PRINT_SERVER_BLOCKENTITY_MESSAGES) {
                    logger.info("[SERVER] Tracked BlockEntities:" + serverBlockEntities.size() + " Ticked at: " + minecraftServer.getTickCount() + "ticks");
                }

                for (ServerLevel world : minecraftServer.getAllLevels()) {
                    int worldEntities = 0;

                    for (LevelChunk chunk : ((LoadedChunksCache) world).fabric_getLoadedChunks()) {
                        worldEntities += chunk.getBlockEntities().size();
                    }

                    if (PRINT_SERVER_BLOCKENTITY_MESSAGES) {
                        logger.info("[SERVER] Tracked BlockEntities in " + world.dimension() + " - " + worldEntities);
                    }

                    entities += worldEntities;
                }

                if (PRINT_SERVER_BLOCKENTITY_MESSAGES) {
                    logger.info("[SERVER] Actual Total BlockEntities: " + entities);
                }

                if (entities != serverBlockEntities.size()) {
                    // Always print mismatches
                    logger.error("[SERVER] Mismatch in tracked blockentities and actual blockentities");
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(minecraftServer -> {
            logger.info("[SERVER] Disconnected. Tracking: " + serverBlockEntities.size() + " blockentities");

            if (serverBlockEntities.size() != 0) {
                logger.error("[SERVER] Mismatch in tracked blockentities, expected 0");
            }
        });
    }
}
