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

package net.fabricmc.fabric.test.rendering;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.test.rendering.client.ArmorRenderingTests;
import net.fabricmc.fabric.test.rendering.client.DimensionalRenderingTest;
import net.fabricmc.fabric.test.rendering.client.FeatureRendererTest;
import net.fabricmc.fabric.test.rendering.client.HudAndShaderTest;
import net.fabricmc.fabric.test.rendering.client.TooltipComponentTests;
import net.fabricmc.fabric.test.rendering.client.WorldRenderEventsTests;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

@Mod(TooltipComponentTestInit.MODID)
public class TooltipComponentTestInit {
    public static final String MODID = "fabric_rendering_v1_testmod";
    private static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);
    public static final RegistryObject<Codec<? extends ChunkGenerator>> VOID_CHUNK_GENERATOR = CHUNK_GENERATORS.register("void", () -> VoidChunkGenerator.CODEC);

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> CUSTOM_TOOLTIP_ITEM = ITEMS.register("custom_tooltip", CustomTooltipItem::new);
    public static final RegistryObject<Item> CUSTOM_ARMOR_ITEM = ITEMS.register("test_chest", () -> new ArmorItem(TestArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public TooltipComponentTestInit() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        CHUNK_GENERATORS.register(bus);
        ITEMS.register(bus);
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ArmorRenderingTests.onInitializeClient();
            DimensionalRenderingTest.onInitializeClient();
            FeatureRendererTest.onInitializeClient();
            HudAndShaderTest.onInitializeClient();
            TooltipComponentTests.onInitializeClient();
            WorldRenderEventsTests.onInitializeClient();
        }
    }

    private static class CustomTooltipItem extends Item {
        CustomTooltipItem() {
            super(new Properties());
        }

        @Override
        public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
            return Optional.of(new Data(stack.getDescriptionId()));
        }
    }

    public record Data(String string) implements TooltipComponent {
    }

    public static final class TestArmorMaterial implements ArmorMaterial {
        public static final TestArmorMaterial INSTANCE = new TestArmorMaterial();

        private TestArmorMaterial() {
        }

        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return 0;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return 0;
        }

        @Override
        public int getEnchantmentValue() {
            return 0;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.LEATHER);
        }

        @Override
        public String getName() {
            return TooltipComponentTestInit.MODID + ":test";
        }

        @Override
        public float getToughness() {
            return 0;
        }

        @Override
        public float getKnockbackResistance() {
            return 0;
        }
    }
}
