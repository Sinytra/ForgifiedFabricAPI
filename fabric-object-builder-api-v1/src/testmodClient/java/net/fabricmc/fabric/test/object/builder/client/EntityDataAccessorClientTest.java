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

package net.fabricmc.fabric.test.object.builder.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.test.object.builder.EntityDataAccessorTest;

public class EntityDataAccessorClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		IEventBus bus = ModLoadingContext.get().getActiveContainer().getEventBus();
		bus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
			event.registerEntityRenderer(EntityDataAccessorTest.TRACK_STACK_ENTITY.get(), TrackStackEntityRenderer::new);
		});
	}
}
