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

package net.fabricmc.fabric.impl.object.builder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class TradeOfferInternals {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-object-builder-api-v1");

    private static final Map<VillagerProfession, Int2ObjectMap<List<VillagerTrades.ItemListing>>> VILLAGER_TRADES = new HashMap<>();

    private static final int LEVEL_GENERIC = 1;
    private static final int LEVEL_RARE = 2;
    private static final Int2ObjectMap<List<VillagerTrades.ItemListing>> WANDERING_TRADER_TRADES = new Int2ObjectOpenHashMap<>();

    private TradeOfferInternals() {
    }

    @SubscribeEvent
    public static void registerVillagerTrades(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = VILLAGER_TRADES.get(event.getType());
        if (trades != null) {
            event.getTrades().putAll(trades);
        }
    }

    @SubscribeEvent
    public static void registerWandererTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> generic = WANDERING_TRADER_TRADES.get(LEVEL_GENERIC);
        if (generic != null) event.getGenericTrades().addAll(generic);

        List<VillagerTrades.ItemListing> rare = WANDERING_TRADER_TRADES.get(LEVEL_RARE);
        if (rare != null) event.getGenericTrades().addAll(rare);
    }

    // synchronized guards against concurrent modifications - Vanilla does not mutate the underlying arrays (as of 1.16),
    // so reads will be fine without locking.
    public static synchronized void registerVillagerOffers(VillagerProfession profession, int level, Consumer<List<VillagerTrades.ItemListing>> factory) {
        Objects.requireNonNull(profession, "VillagerProfession may not be null.");
        registerOffers(VILLAGER_TRADES.computeIfAbsent(profession, p -> new Int2ObjectOpenHashMap<>()), level, factory);
    }

    public static synchronized void registerWanderingTraderOffers(int level, Consumer<List<VillagerTrades.ItemListing>> factory) {
        registerOffers(WANDERING_TRADER_TRADES, level, factory);
    }

    // Shared code to register offers for both villagers and wandering traders.
    private static void registerOffers(Int2ObjectMap<List<VillagerTrades.ItemListing>> leveledTradeMap, int level, Consumer<List<VillagerTrades.ItemListing>> factory) {
        final List<VillagerTrades.ItemListing> list = new ArrayList<>();
        factory.accept(list);

        final List<VillagerTrades.ItemListing> originalEntries = leveledTradeMap.computeIfAbsent(level, key -> new ArrayList<>());
        originalEntries.addAll(list);
    }

    public static void printRefreshOffersWarning() {
        Throwable loggingThrowable = new Throwable();
        LOGGER.warn("TradeOfferHelper#refreshOffers does not do anything, yet it was called! Stack trace:", loggingThrowable);
    }
}
