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

package net.fabricmc.fabric.test.screenhandler;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.test.screenhandler.block.BoxBlock;
import net.fabricmc.fabric.test.screenhandler.block.BoxBlockEntity;
import net.fabricmc.fabric.test.screenhandler.client.ClientScreenHandlerTest;
import net.fabricmc.fabric.test.screenhandler.item.BagItem;
import net.fabricmc.fabric.test.screenhandler.item.PositionedBagItem;
import net.fabricmc.fabric.test.screenhandler.screen.BagScreenHandler;
import net.fabricmc.fabric.test.screenhandler.screen.BoxScreenHandler;
import net.fabricmc.fabric.test.screenhandler.screen.PositionedBagScreenHandler;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ScreenHandlerTest.ID)
public class ScreenHandlerTest {
    public static final String ID = "fabric_screen_handler_api_v1_testmod";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ID);

    public static final RegistryObject<Item> BAG = ITEMS.register("bag", () -> new BagItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> POSITIONED_BAG = ITEMS.register("positioned_bag", () -> new PositionedBagItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Block> BOX = BLOCKS.register("box", () -> new BoxBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD)));
    public static final RegistryObject<Item> BOX_ITEM = ITEMS.register("box", () -> new BlockItem(BOX.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<BoxBlockEntity>> BOX_ENTITY = BLOCK_ENTITY_TYPES.register("box_entity", () -> FabricBlockEntityTypeBuilder.create(BoxBlockEntity::new, BOX.get()).build());
    public static final RegistryObject<MenuType<BagScreenHandler>> BAG_SCREEN_HANDLER = MENU_TYPES.register("bag_screen_handler", () -> new MenuType<>(BagScreenHandler::new, FeatureFlags.VANILLA_SET));
    public static final RegistryObject<MenuType<PositionedBagScreenHandler>> POSITIONED_BAG_SCREEN_HANDLER = MENU_TYPES.register("positioned_bag_screen_handler", () -> new ExtendedScreenHandlerType<>(PositionedBagScreenHandler::new));
    public static final RegistryObject<MenuType<BoxScreenHandler>> BOX_SCREEN_HANDLER = MENU_TYPES.register("box_screen_handler", () -> new ExtendedScreenHandlerType<>(BoxScreenHandler::new));

    public ScreenHandlerTest() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        MENU_TYPES.register(bus);
        if (FMLLoader.getDist() == Dist.CLIENT) {
            bus.addListener(ClientScreenHandlerTest::onInitializeClient);
        }
    }
}
