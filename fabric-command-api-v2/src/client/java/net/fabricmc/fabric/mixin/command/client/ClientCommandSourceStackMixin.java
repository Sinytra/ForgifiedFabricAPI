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

import net.minecraftforge.client.ClientCommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

@Mixin(ClientCommandSourceStack.class)
abstract class ClientCommandSourceStackMixin implements FabricClientCommandSource {

	@Override
	public void sendFeedback(Text message) {
		getClient().inGameHud.getChatHud().addMessage(message);
		getClient().getNarratorManager().narrate(message);
	}

	@Override
	public void sendError(Text message) {
		sendFeedback(Text.literal("").append(message).formatted(Formatting.RED));
	}

	@Override
	public MinecraftClient getClient() {
		return MinecraftClient.getInstance();
	}

	@Override
	public ClientPlayerEntity getPlayer() {
		return getClient().player;
	}

	@Override
	public ClientWorld getWorld() {
		return getClient().world;
	}
}
