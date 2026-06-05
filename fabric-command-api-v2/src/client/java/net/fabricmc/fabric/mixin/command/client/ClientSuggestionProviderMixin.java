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

package net.fabricmc.fabric.mixin.command.client;

import net.neoforged.neoforge.client.ClientCommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

@Mixin({ClientSuggestionProvider.class, ClientCommandSourceStack.class})
abstract class ClientSuggestionProviderMixin implements FabricClientCommandSource {
	@Override
	public void sendFeedback(Component message) {
		getClient().gui.getChat().addClientSystemMessage(message);
		getClient().getNarrator().saySystemChatQueued(message);
	}

	@Override
	public void sendError(Component message) {
		sendFeedback(Component.empty().append(message).withStyle(ChatFormatting.RED));
	}

	@Override
	public Minecraft getClient() {
		return Minecraft.getInstance();
	}

	@Override
	public LocalPlayer getPlayer() {
		return getClient().player;
	}

	@Override
	public ClientLevel getLevel() {
		return getClient().level;
	}
}
