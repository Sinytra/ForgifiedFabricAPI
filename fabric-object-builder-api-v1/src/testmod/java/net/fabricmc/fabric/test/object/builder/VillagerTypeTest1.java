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

package net.fabricmc.fabric.test.object.builder;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.entity;
import static net.minecraft.commands.arguments.EntityArgument.getEntity;

public class VillagerTypeTest1 {

	public static void onInitialize() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.NETHERITE_SCRAP, 4), new ItemStack(Items.NETHERITE_INGOT), 2, 6, 0.15F)));
		});

		TradeOfferHelper.registerWanderingTraderOffers(1, factories -> {
			factories.add(new SimpleTradeFactory(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.NETHERITE_SCRAP, 4), new ItemStack(Items.NETHERITE_INGOT), 2, 6, 0.35F)));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("fabric_applywandering_trades")
					.then(argument("entity", entity()).executes(context -> {
						final Entity entity = getEntity(context, "entity");

						if (!(entity instanceof WanderingTrader trader)) {
							throw new SimpleCommandExceptionType(Component.literal("Entity is not a wandering trader")).create();
						}

						trader.getOffers().clear();

						for (VillagerTrades.ItemListing[] value : VillagerTrades.WANDERING_TRADER_TRADES.values()) {
							for (VillagerTrades.ItemListing factory : value) {
								final MerchantOffer result = factory.getOffer(trader, RandomSource.create());

								if (result == null) {
									continue;
								}

								trader.getOffers().add(result);
							}
						}

						return 1;
					})));
		});
	}
}
