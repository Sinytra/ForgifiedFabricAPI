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

package net.fabricmc.fabric.test.networking;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.test.networking.channeltest.NetworkingChannelTest;
import net.fabricmc.fabric.test.networking.client.DisconnectScreenTest;
import net.fabricmc.fabric.test.networking.client.channeltest.NetworkingChannelClientTest;
import net.fabricmc.fabric.test.networking.client.keybindreciever.NetworkingKeybindClientPacketTest;
import net.fabricmc.fabric.test.networking.client.login.NetworkingLoginQueryClientTest;
import net.fabricmc.fabric.test.networking.client.play.NetworkingPlayPacketClientTest;
import net.fabricmc.fabric.test.networking.keybindreciever.NetworkingKeybindPacketTest;
import net.fabricmc.fabric.test.networking.login.NetworkingLoginQueryTest;
import net.fabricmc.fabric.test.networking.play.NetworkingPlayPacketTest;

@Mod(NetworkingTestmods.ID)
public final class NetworkingTestmods {
	public static final String ID = "fabric_networking_api_v1_testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static Identifier id(String name) {
		return new Identifier(ID, name);
	}

	public NetworkingTestmods() {
		if (FMLLoader.getDist() == Dist.CLIENT) {
			DisconnectScreenTest.onInitializeClient();
			new NetworkingPlayPacketClientTest().onInitializeClient();
			NetworkingLoginQueryClientTest.onInitializeClient();
			NetworkingKeybindClientPacketTest.onInitializeClient();
			new NetworkingChannelClientTest().onInitializeClient();
		}
		NetworkingPlayPacketTest.onInitialize();
		new NetworkingLoginQueryTest().onInitialize();
		NetworkingKeybindPacketTest.onInitialize();
		NetworkingChannelTest.onInitialize();
	}
}
