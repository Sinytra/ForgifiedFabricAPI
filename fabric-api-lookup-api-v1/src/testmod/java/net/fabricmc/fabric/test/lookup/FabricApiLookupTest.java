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

package net.fabricmc.fabric.test.lookup;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.test.lookup.api.ItemApis;
import net.fabricmc.fabric.test.lookup.api.ItemInsertable;
import net.fabricmc.fabric.test.lookup.client.entity.FabricEntityApiLookupTestClient;
import net.fabricmc.fabric.test.lookup.compat.InventoryExtractableProvider;
import net.fabricmc.fabric.test.lookup.compat.InventoryInsertableProvider;
import net.fabricmc.fabric.test.lookup.entity.FabricEntityApiLookupTest;
import net.fabricmc.fabric.test.lookup.item.FabricItemApiLookupTest;

@Mod("fabric_api_lookup_api_v1_testmod")
public class FabricApiLookupTest {
	public static final String MOD_ID = "fabric-lookup-api-v1-testmod";

	public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
	public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

	// Chute - Block without model that transfers item from the container above to the container below.
	// It's meant to work with unsided containers: chests, dispensers, droppers and hoppers.
	public static final RegistryObject<ChuteBlock> CHUTE_BLOCK = BLOCK_REGISTER.register("chute", () -> new ChuteBlock(FabricBlockSettings.of(Material.METAL)));
	public static final RegistryObject<BlockItem> CHUTE_ITEM = ITEM_REGISTER.register("chute", () -> new BlockItem(CHUTE_BLOCK.get(), new Item.Settings()));
	public static final RegistryObject<BlockEntityType<ChuteBlockEntity>> CHUTE_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_REGISTER.register("chute", () -> FabricBlockEntityTypeBuilder.create(ChuteBlockEntity::new, CHUTE_BLOCK.get()).build());
	// Cobble gen - Block without model that can generate infinite cobblestone when placed above a chute.
	// It's meant to test BlockApiLookup#registerSelf.
	public static final RegistryObject<CobbleGenBlock> COBBLE_GEN_BLOCK = BLOCK_REGISTER.register("cobble_gen", () -> new CobbleGenBlock(FabricBlockSettings.of(Material.METAL)));
	public static final RegistryObject<BlockItem> COBBLE_GEN_ITEM = ITEM_REGISTER.register("cobble_gen", () -> new BlockItem(COBBLE_GEN_BLOCK.get(), new Item.Settings()));
	public static final RegistryObject<BlockEntityType<CobbleGenBlockEntity>> COBBLE_GEN_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_REGISTER.register("cobble_gen", () -> FabricBlockEntityTypeBuilder.create(CobbleGenBlockEntity::new, COBBLE_GEN_BLOCK.get()).build());
	// Testing for item api lookups is done in the `item` package.

	public static final RegistryObject<InspectorBlock> INSPECTOR_BLOCK = BLOCK_REGISTER.register("inspector", () -> new InspectorBlock(FabricBlockSettings.of(Material.METAL)));
	public static final RegistryObject<BlockItem> INSPECTOR_ITEM = ITEM_REGISTER.register("inspector", () -> new BlockItem(INSPECTOR_BLOCK.get(), new Item.Settings()));

	public FabricApiLookupTest() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::onCommonSetup);
		bus.addListener(FabricEntityApiLookupTest::onAttributesCreate);
		bus.addListener(FabricEntityApiLookupTestClient::onInitializeClient);

		BLOCK_REGISTER.register(bus);
		ITEM_REGISTER.register(bus);
		BLOCK_ENTITY_REGISTER.register(bus);
		ENTITY_TYPE_REGISTER.register(bus);

		FabricItemApiLookupTest.onInitialize();
		FabricEntityApiLookupTest.onInitialize();
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		InventoryExtractableProvider extractableProvider = new InventoryExtractableProvider();
		InventoryInsertableProvider insertableProvider = new InventoryInsertableProvider();

		ItemApis.INSERTABLE.registerForBlockEntities(insertableProvider, BlockEntityType.CHEST, BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.HOPPER);
		ItemApis.EXTRACTABLE.registerForBlockEntities(extractableProvider, BlockEntityType.CHEST, BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.HOPPER);
		ItemApis.EXTRACTABLE.registerSelf(COBBLE_GEN_BLOCK_ENTITY_TYPE.get());

		testLookupRegistry();
		testSelfRegistration();

		FabricItemApiLookupTest.runTests();
		FabricEntityApiLookupTest.runTests();
	}

	private static void testLookupRegistry() {
		BlockApiLookup<ItemInsertable, @NotNull Direction> insertable2 = BlockApiLookup.get(new Identifier("testmod:item_insertable"), ItemInsertable.class, Direction.class);

		if (insertable2 != ItemApis.INSERTABLE) {
			throw new AssertionError("The registry should have returned the same instance.");
		}

		ensureException(() -> {
			BlockApiLookup<Void, Void> wrongInsertable = BlockApiLookup.get(new Identifier("testmod:item_insertable"), Void.class, Void.class);
			wrongInsertable.registerFallback((world, pos, state, be, nocontext) -> null);
		}, "The registry should have prevented creation of another instance with different classes, but same id.");

		if (!insertable2.getId().equals(new Identifier("testmod:item_insertable"))) {
			throw new AssertionError("Incorrect identifier was returned.");
		}

		if (insertable2.apiClass() != ItemInsertable.class) {
			throw new AssertionError("Incorrect API class was returned.");
		}

		if (insertable2.contextClass() != Direction.class) {
			throw new AssertionError("Incorrect context class was returned.");
		}
	}

	private static void testSelfRegistration() {
		ensureException(() -> {
			ItemApis.INSERTABLE.registerSelf(COBBLE_GEN_BLOCK_ENTITY_TYPE.get());
		}, "The BlockApiLookup should have prevented self-registration of incompatible block entity types.");
	}

	public static void ensureException(Runnable runnable, String message) {
		boolean failed = false;

		try {
			runnable.run();
		} catch (Throwable t) {
			failed = true;
		}

		if (!failed) {
			throw new AssertionError(message);
		}
	}
}
