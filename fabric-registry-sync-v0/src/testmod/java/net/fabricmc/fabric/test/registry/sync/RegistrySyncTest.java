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

package net.fabricmc.fabric.test.registry.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.fabricmc.fabric.test.registry.sync.client.DynamicRegistryClientTest;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;

@Mod("fabric_registry_sync_v0_v1_testmod")
public class RegistrySyncTest {
	private static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * These are system property's as it allows for easier testing with different run configurations.
	 */
	public static final boolean REGISTER_BLOCKS = Boolean.parseBoolean(System.getProperty("fabric.registry.sync.test.register.blocks", "true"));
	public static final boolean REGISTER_ITEMS = Boolean.parseBoolean(System.getProperty("fabric.registry.sync.test.register.items", "true"));

	private static final List<DeferredRegister<?>> REGISTERS = new ArrayList<>();
	private static final RegistryKey<Registry<String>> FABRIC_REGISTRY_KEY = RegistryKey.ofRegistry(new Identifier("registry_sync", "fabric_registry"));
	private static SimpleRegistry<String> fabricRegistry;

	static {
		if (REGISTER_BLOCKS) {
			// For checking raw id bulk in direct registry packet, make registry_sync namespace have two bulks.
			registerBlocks("registry_sync", 5, 0);
			registerBlocks("registry_sync2", 50, 0);
			registerBlocks("registry_sync", 2, 5);
		}
	}

	public RegistrySyncTest() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		REGISTERS.forEach(r -> r.register(bus));
		bus.addListener(RegistrySyncTest::onCommonSetup);
		bus.addListener(RegistrySyncTest::onRegister);
		fabricRegistry = FabricRegistryBuilder.createSimple(FABRIC_REGISTRY_KEY)
				.attribute(RegistryAttribute.SYNCED)
				.buildAndRegister();
		CustomDynamicRegistryTest.onInitialize();
		if (FMLLoader.getDist().isClient()) {
			DynamicRegistryClientTest.onInitializeClient();
		}
	}

	private static void onRegister(RegisterEvent event) {
		event.register(FABRIC_REGISTRY_KEY, helper -> {
			helper.register(new Identifier("registry_sync", "test"), "test");
		});
	}

	public static void onCommonSetup(FMLCommonSetupEvent event) {
		if (REGISTER_BLOCKS) {
			Validate.isTrue(!RegistryAttributeHolder.get(Registries.BLOCK).hasAttribute(RegistryAttribute.MODDED), "Modded block was registered but registry marked as modded");

			if (REGISTER_ITEMS) {
				Validate.isTrue(!RegistryAttributeHolder.get(Registries.ITEM).hasAttribute(RegistryAttribute.MODDED), "Modded item was registered but registry marked as modded");
			}
		}

		Validate.isTrue(Registries.REGISTRIES.getIds().contains(new Identifier("registry_sync", "fabric_registry")));

		Validate.isTrue(RegistryAttributeHolder.get(fabricRegistry).hasAttribute(RegistryAttribute.MODDED));
		Validate.isTrue(RegistryAttributeHolder.get(fabricRegistry).hasAttribute(RegistryAttribute.CUSTOM));
		Validate.isTrue(RegistryAttributeHolder.get(fabricRegistry).hasAttribute(RegistryAttribute.SYNCED));
		Validate.isTrue(!RegistryAttributeHolder.get(fabricRegistry).hasAttribute(RegistryAttribute.PERSISTED));

		final AtomicBoolean setupCalled = new AtomicBoolean(false);

		DynamicRegistrySetupCallback.EVENT.register(registryManager -> {
			setupCalled.set(true);
			registryManager.registerEntryAdded(RegistryKeys.BIOME, (rawId, id, object) -> {
				LOGGER.info("Biome added: {}", id);
			});
		});

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			if (!setupCalled.get()) {
				throw new IllegalStateException("DRM setup was not called before startup!");
			}
		});

		// Vanilla status effects don't have an entry for the int id 0, test we can handle this.
		RegistryAttributeHolder.get(Registries.STATUS_EFFECT).addAttribute(RegistryAttribute.MODDED);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("remote_remap_error_test").executes(context -> {
					Map<Identifier, Object2IntMap<Identifier>> registryData = Map.of(
							RegistryKeys.BLOCK.getValue(), createFakeRegistryEntries(),
							RegistryKeys.ITEM.getValue(), createFakeRegistryEntries()
					);

					try {
						RegistrySyncManager.checkRemoteRemap(registryData);
					} catch (RemapException e) {
						final ServerPlayerEntity player = context.getSource().getPlayer();

						if (player != null) {
							player.networkHandler.disconnect(Objects.requireNonNull(e.getText()));
						}

						return 1;
					}

					throw new IllegalStateException();
				})));
	}

	private static void registerBlocks(String namespace, int amount, int startingId) {
		DeferredRegister<Block> blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, namespace);
		DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, namespace);
		REGISTERS.add(blocks);
		REGISTERS.add(items);

		for (int i = 0; i < amount; i++) {
			RegistryObject<Block> block = blocks.register("block_" + (i + startingId), () -> new Block(AbstractBlock.Settings.create()));

			if (REGISTER_ITEMS) {
				items.register("block_" + (i + startingId), () -> new BlockItem(block.get(), new Item.Settings()));
			}
		}
	}

	private static Object2IntMap<Identifier> createFakeRegistryEntries() {
		Object2IntMap<Identifier> map = new Object2IntOpenHashMap<>();

		for (int i = 0; i < 12; i++) {
			map.put(new Identifier("mod_" + i, "entry"), 0);
		}

		return map;
	}
}
