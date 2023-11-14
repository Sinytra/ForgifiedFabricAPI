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

package net.fabricmc.fabric.impl.message;

import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;

import net.minecraft.text.Text;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.server.network.ServerPlayerEntity;

@Mod("fabric_message_api_v1")
public class MessagesImpl {

	public MessagesImpl() {
		MinecraftForge.EVENT_BUS.addListener(MessagesImpl::onServerChatSubmitted);
    }

	private static void onServerChatSubmitted(ServerChatEvent event) {
		ServerPlayerEntity sender = event.getPlayer();
		Text message = event.getMessage();
		Text processed =  ServerMessageDecoratorEvent.EVENT.invoker().decorate(sender, message).join();
		event.setMessage(processed);
	}
}
