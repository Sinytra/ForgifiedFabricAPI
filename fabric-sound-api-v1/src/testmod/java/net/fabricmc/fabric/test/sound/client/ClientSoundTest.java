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

package net.fabricmc.fabric.test.sound.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;

/**
 * Plays a sine wave when the {@code /sine} client command is run.
 */
@Mod(ClientSoundTest.MOD_ID)
public class ClientSoundTest {
	public static final String MOD_ID = "fabric_sound_api_v1_testmod";

	public ClientSoundTest() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
			dispatcher.register(ClientCommandManager.literal("sine").executes(o -> {
				Minecraft client = o.getSource().getClient();
				client.getSoundManager().play(new SineSound(client.player.position()));
				return 0;
			}));
		});
	}
}
