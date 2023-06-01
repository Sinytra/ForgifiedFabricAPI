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

package net.fabricmc.fabric.test.lookup.item;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.test.lookup.FabricApiLookupTest;
import net.fabricmc.fabric.test.lookup.api.Inspectable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.registries.RegistryObject;

import static net.fabricmc.fabric.test.lookup.FabricApiLookupTest.ensureException;

public class FabricItemApiLookupTest {
    public static final ItemApiLookup<Inspectable, Void> INSPECTABLE = ItemApiLookup.get(new ResourceLocation("testmod:inspectable"), Inspectable.class, Void.class);

    public static final RegistryObject<InspectableItem> HELLO_ITEM = FabricApiLookupTest.ITEM_REGISTER.register("hello", () -> new InspectableItem("Hello Fabric API tester!"));

	public static void onInitialize() {}
	
    public static void runTests() {
        // Diamonds and diamond blocks can be inspected and will also print their name.
        INSPECTABLE.registerForItems((stack, ignored) -> () -> {
            if (stack.hasCustomHoverName()) {
                return stack.getHoverName();
            } else {
                return Component.literal("Unnamed gem.");
            }
        }, Items.DIAMOND, Items.DIAMOND_BLOCK);
        // Test registerSelf
        INSPECTABLE.registerSelf(HELLO_ITEM.get());
        // Tools report their mining level
        INSPECTABLE.registerFallback((stack, ignored) -> {
            Item item = stack.getItem();

            if (item instanceof TieredItem tieredItem) {
                return () -> Component.literal("Tool mining level: " + tieredItem.getTier().getLevel());
            } else {
                return null;
            }
        });

        testSelfRegistration();
    }

    private static void testSelfRegistration() {
        ensureException(() -> {
            INSPECTABLE.registerSelf(Items.WATER_BUCKET);
        }, "The ItemApiLookup should have prevented self-registration of incompatible items.");
    }
}
