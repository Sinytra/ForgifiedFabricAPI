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

package net.fabricmc.fabric.test.event.lifecycle;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

import net.fabricmc.fabric.test.event.lifecycle.client.ClientBlockEntityLifecycleTests;
import net.fabricmc.fabric.test.event.lifecycle.client.ClientEntityLifecycleTests;
import net.fabricmc.fabric.test.event.lifecycle.client.ClientLifecycleTests;
import net.fabricmc.fabric.test.event.lifecycle.client.ClientTickTests;

@Mod("fabric_lifecycle_events_v1_testmod")
public class LifecycleEventsTestMod {

	public LifecycleEventsTestMod() {
		CommonLifecycleTests.onInitialize();
		new ServerBlockEntityLifecycleTests().onInitialize();
		new ServerEntityLifecycleTests().onInitialize();
		new ServerLifecycleTests().onInitialize();
		new ServerResourceReloadTests().onInitialize();
		new ServerTickTests().onInitialize();
		if (FMLLoader.getDist() == Dist.CLIENT) {
			new ClientBlockEntityLifecycleTests().onInitializeClient();
			new ClientEntityLifecycleTests().onInitializeClient();
			new ClientLifecycleTests().onInitializeClient();
			new ClientTickTests().onInitializeClient();
		}
	}
}
