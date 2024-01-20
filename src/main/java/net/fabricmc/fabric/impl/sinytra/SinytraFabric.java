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

package net.fabricmc.fabric.impl.sinytra;

import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePackProfile;

import net.minecraft.resource.ResourcePackSource;
import net.minecraft.text.Text;

import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("fabric_api")
public class SinytraFabric {
	public SinytraFabric() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, AddPackFindersEvent.class, this::addPackFinder);
	}

	/**
	 * Some mods (ARRP) may use a resource pack named {@code fabric} to sort their own packs around. So just provide it.
	 */
	private void addPackFinder(AddPackFindersEvent event) {
		event.addRepositorySource(profileAdder -> profileAdder.accept(
				ResourcePackProfile.create(
						"fabric",
						Text.of("fabric"),
						true,
						name -> new DirectoryResourcePack(name, ModList.get().getModContainerById("fabric_api").get().getModInfo().getOwningFile()
								.getFile().findResource("dummyrp"), true),
						event.getPackType(),
						ResourcePackProfile.InsertionPosition.TOP,
						ResourcePackSource.BUILTIN
				)
		));
	}
}
