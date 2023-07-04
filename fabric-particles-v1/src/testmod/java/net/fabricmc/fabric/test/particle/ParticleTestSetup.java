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

package net.fabricmc.fabric.test.particle;

import com.mojang.brigadier.Command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.test.particle.client.ParticleRenderEventTests;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@Mod("fabric_particles_v1_testmod")
public final class ParticleTestSetup {
	public static final String MODID = "fabric-particles-v1-testmod";

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

	// The dust particles of this block are always tinted (default).
	public static final RegistryObject<Block> ALWAYS_TINTED = registerBlock("always_tinted", () -> new ParticleTintTestBlock(AbstractBlock.Settings.create().breakInstantly(), 0xFF00FF));
	// The dust particles of this block are only tinted when the block is broken over water.
	public static final RegistryObject<Block> TINTED_OVER_WATER = registerBlock("tinted_over_water", () -> new ParticleTintTestBlock(AbstractBlock.Settings.create().breakInstantly(), 0xFFFF00));
	// The dust particles of this block are never tinted.
	public static final RegistryObject<Block> NEVER_TINTED = registerBlock("never_tinted", () -> new ParticleTintTestBlock(AbstractBlock.Settings.create().breakInstantly(), 0x00FFFF));

	public ParticleTestSetup() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		BLOCKS.register(bus);
		ITEMS.register(bus);
		if (FMLLoader.getDist() == Dist.CLIENT) {
			bus.addListener(ParticleRenderEventTests::onInitializeClient);
		}

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("addparticletestblocks").executes(context -> {
				PlayerInventory inventory = context.getSource().getPlayer().getInventory();
				inventory.offerOrDrop(new ItemStack(ALWAYS_TINTED.get()));
				inventory.offerOrDrop(new ItemStack(TINTED_OVER_WATER.get()));
				inventory.offerOrDrop(new ItemStack(NEVER_TINTED.get()));
				return Command.SINGLE_SUCCESS;
			}));
		});
	}

	private static RegistryObject<Block> registerBlock(String path, Supplier<Block> block) {
		RegistryObject<Block> registryObject = BLOCKS.register(path, block);
		ITEMS.register(path, () -> new BlockItem(registryObject.get(), new Item.Settings()));
		return registryObject;
	}
}
