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

package net.fabricmc.fabric.test.event.lifecycle.client;

import com.google.common.collect.Iterables;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.test.event.lifecycle.ServerLifecycleTests;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests related to the lifecycle of entities.
 */
public final class ClientEntityLifecycleTests {
    private static final boolean PRINT_CLIENT_ENTITY_MESSAGES = System.getProperty("fabric-lifecycle-events-testmod.printClientEntityMessages") != null;
    private static final List<Entity> clientEntities = new ArrayList<>();
    private static int clientTicks;

    public static void onInitializeClient() {
        final Logger logger = ServerLifecycleTests.LOGGER;

        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            clientEntities.add(entity);

            if (PRINT_CLIENT_ENTITY_MESSAGES) {
                logger.info("[CLIENT]" + " LOADED " + ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString() + " - Entities: " + clientEntities.size());
            }
        });

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            clientEntities.remove(entity);

            if (PRINT_CLIENT_ENTITY_MESSAGES) {
                logger.info("[CLIENT]" + " UNLOADED " + ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString() + " - Entities: " + clientEntities.size());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (clientTicks++ % 200 == 0 && client.level != null) {
                final int entities = Iterables.toArray(client.level.entitiesForRendering(), Entity.class).length;

                if (PRINT_CLIENT_ENTITY_MESSAGES) {
                    logger.info("[CLIENT] Tracked Entities:" + clientEntities.size() + " Ticked at: " + clientTicks + "ticks");
                    logger.info("[CLIENT] Actual Entities: " + entities);
                }

                if (entities != clientEntities.size()) {
                    // Always print mismatches
                    logger.error("[CLIENT] Mismatch in tracked entities and actual entities");
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(minecraftServer -> {
            if (!minecraftServer.isDedicatedServer()) { // fixme: Use ClientNetworking#PLAY_DISCONNECTED instead of the server stop callback for testing.
                logger.info("[CLIENT] Disconnected. Tracking: " + clientEntities.size() + " entities");

                if (clientEntities.size() != 0) {
                    logger.error("[CLIENT] Mismatch in tracked entities, expected 0");
                }
            }
        });
    }
}
