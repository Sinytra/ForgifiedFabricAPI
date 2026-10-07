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

package net.fabricmc.fabric.mixin.client.gametest.config;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.ClientNeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;

@Mixin(ClientNeoForgeMod.class)
public class ClientNeoForgeModMixin {
	@WrapWithCondition(method = "lambda$new$0", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/config/ConfigTracker;unloadConfigs(Lnet/neoforged/fml/config/ModConfig$Type;)V"))
	private static boolean keepInProcessServerConfigs(ConfigTracker tracker, ModConfig.Type type) {
		return !ThreadingImpl.isServerRunning;
	}
}
