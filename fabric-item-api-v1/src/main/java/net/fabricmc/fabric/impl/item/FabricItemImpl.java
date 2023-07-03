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

package net.fabricmc.fabric.impl.item;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;
import net.fabricmc.fabric.impl.client.item.ItemApiClientEventHooks;

@Mod("fabric_item_api_v1")
public class FabricItemImpl {

	public FabricItemImpl() {
		if (FMLLoader.getDist() == Dist.CLIENT) {
			MinecraftForge.EVENT_BUS.register(ItemApiClientEventHooks.class);
		}
		MinecraftForge.EVENT_BUS.addListener(FabricItemImpl::modifyItemAttributeModifiers);
	}

	private static void modifyItemAttributeModifiers(ItemAttributeModifierEvent event) {
		ModifyItemAttributeModifiersCallback.EVENT.invoker().modifyAttributeModifiers(event.getItemStack(), event.getSlotType(), event.getModifiers());
	}
}
