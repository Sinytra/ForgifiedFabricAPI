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

import com.google.common.collect.Iterables;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests related to the lifecycle of entities.
 */
public final class ServerEntityLifecycleTests {
    private static final boolean PRINT_SERVER_ENTITY_MESSAGES = System.getProperty("fabric-lifecycle-events-testmod.printServerEntityMessages") != null;
    private static final List<Entity> serverEntities = new ArrayList<>();
    private static int serverTicks = 0;

    public static void onInitialize() {
        final Logger logger = ServerLifecycleTests.LOGGER;

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            serverEntities.add(entity);

            if (PRINT_SERVER_ENTITY_MESSAGES) {
                logger.info("[SERVER] LOADED " + entity.toString() + " - Entities: " + serverEntities.size());
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            serverEntities.remove(entity);

            if (PRINT_SERVER_ENTITY_MESSAGES) {
                logger.info("[SERVER] UNLOADED " + entity.toString() + " - Entities: " + serverEntities.size());
            }
        });

        ServerEntityEvents.EQUIPMENT_CHANGE.register((livingEntity, equipmentSlot, previousStack, currentStack) -> {
            if (PRINT_SERVER_ENTITY_MESSAGES) {
                logger.info("[SERVER] Entity equipment change: Entity: {}, Slot {}, Previous: {}, Current {} ", livingEntity, equipmentSlot.name(), previousStack, currentStack);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (serverTicks++ % 200 == 0) {
                int entities = 0;

                for (ServerLevel world : server.getAllLevels()) {
                    final int worldEntities = Iterables.size(world.getAllEntities());

                    if (PRINT_SERVER_ENTITY_MESSAGES) {
                        logger.info("[SERVER] Tracked Entities in " + world.dimension() + " - " + worldEntities);
                    }

                    entities += worldEntities;
                }

                if (PRINT_SERVER_ENTITY_MESSAGES) {
                    logger.info("[SERVER] Actual Total Entities: " + entities);
                }

                if (entities != serverEntities.size()) {
                    // Always print mismatches
                    logger.error("[SERVER] Mismatch in tracked entities and actual entities");
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            logger.info("[SERVER] Disconnected. Tracking: " + serverEntities.size() + " entities");

            if (serverEntities.size() != 0) {
                logger.error("[SERVER] Mismatch in tracked entities, expected 0");
            }
        });
    }
}
