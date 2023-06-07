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

package net.fabricmc.fabric.test.item.group;

import com.google.common.base.Supplier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.IdentifiableItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

@Mod(ItemGroupTest.MODID)
public class ItemGroupTest {
    public static final String MODID = "fabric_item_group_api_v1_testmod";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final RegistryObject<Item> TEST_ITEM = ITEMS.register("item_test_group", () -> new Item(new Item.Properties()));

    //Adds an item group with all items in it
    private static final CreativeModeTab ITEM_GROUP = FabricItemGroup.builder(new ResourceLocation(MODID, "test_group"))
        .title(Component.literal("Test Item Group"))
        .icon(() -> new ItemStack(Items.DIAMOND))
        .displayItems((context, entries) -> {
            entries.acceptAll(BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(input -> !input.isEmpty())
                .toList());
        })
        .build();

    public ItemGroupTest() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(content -> {
            content.accept(TEST_ITEM.get());

            content.addBefore(Blocks.OAK_FENCE, Items.DIAMOND, Items.DIAMOND_BLOCK);
            content.addAfter(Blocks.OAK_DOOR, Items.EMERALD, Items.EMERALD_BLOCK);

            // Test adding when the existing entry does not exist.
            content.addBefore(Blocks.BEDROCK, Items.GOLD_INGOT, Items.GOLD_BLOCK);
            content.addAfter(Blocks.BEDROCK, Items.IRON_INGOT, Items.IRON_BLOCK);
        });

        // Add a differently damaged pickaxe to all groups
        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, content) -> {
            ItemStack minDmgPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
            minDmgPickaxe.setDamageValue(1);
            content.prepend(minDmgPickaxe);

            ItemStack maxDmgPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
            maxDmgPickaxe.setDamageValue(maxDmgPickaxe.getMaxDamage() - 1);
            content.accept(maxDmgPickaxe);
        });

        for (int i = 0; i < 100; i++) {
            final int index = i;
            FabricItemGroup.builder(new ResourceLocation(MODID, "test_group_" + i))
                .title(Component.literal("Test Item Group: " + i))
                .icon((Supplier<ItemStack>) () -> new ItemStack(BuiltInRegistries.BLOCK.byId(index)))
                .displayItems((context, entries) -> {
                    var itemStack = new ItemStack(BuiltInRegistries.ITEM.byId(index));

                    if (!itemStack.isEmpty()) {
                        entries.accept(itemStack);
                    }
                })
                .build();
        }

        String hotbarId = ((IdentifiableItemGroup) CreativeModeTabs.HOTBAR).getId().toString();
        if (!Objects.equals(hotbarId, "minecraft:hotbar")) {
            throw new RuntimeException("Invalid id for hobar creative tab, expected minecraft:hotbar, got " + hotbarId);
        }

        String expected = MODID + ":test_group";
        String testItemGroupId = ((IdentifiableItemGroup) ITEM_GROUP).getId().toString();
        if (!Objects.equals(testItemGroupId, expected)) {
            throw new RuntimeException("Invalid id for hobar creative tab, expected " + expected + ", got " + testItemGroupId);
        }
    }
}
