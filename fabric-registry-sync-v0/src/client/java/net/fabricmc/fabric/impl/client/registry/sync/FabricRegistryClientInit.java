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

package net.fabricmc.fabric.impl.client.registry.sync;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistryPacketHandler;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricRegistryClientInit {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricRegistryClientInit.class);

	public static void onInitializeClient() {
		registerSyncPacketReceiver(RegistrySyncManager.DIRECT_PACKET_HANDLER);
	}

	private static void registerSyncPacketReceiver(RegistryPacketHandler packetHandler) {
		ClientPlayNetworking.registerGlobalReceiver(packetHandler.getPacketId(), (client, handler, buf, responseSender) ->
				RegistrySyncManager.receivePacket(client, packetHandler, buf, RegistrySyncManager.DEBUG || !client.isLocalServer(), (e) -> {
					LOGGER.error("Registry remapping failed!", e);
					client.execute(() -> handler.getConnection().disconnect(getText(e)));
				}));
	}

	private static Component getText(Exception e) {
		if (e instanceof RemapException remapException) {
			final Component text = remapException.getText();

			if (text != null) {
				return text;
			}
		}

		return Component.literal("Registry remapping failed: " + e.getMessage());
	}
}
