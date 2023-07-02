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

package net.fabricmc.fabric.test.resource.loader;

import java.util.Collection;
import java.util.Collections;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class ResourceReloadListenerTestMod {
	public static final String MODID = "fabric-resource-loader-v0-testmod";

	private static boolean clientResources = false;
	private static boolean serverResources = false;

	public static void onInitialize() {
		setupClientReloadListeners();
		setupServerReloadListeners();

		ServerTickEvents.START_WORLD_TICK.register(world -> {
			if (!clientResources && FMLLoader.getDist() == Dist.CLIENT) {
				throw new AssertionError("Client reload listener was not called.");
			}

			if (!serverResources) {
				throw new AssertionError("Server reload listener was not called.");
			}
		});
	}

	private static void setupClientReloadListeners() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier(MODID, "client_second");
			}

			@Override
			public void reload(ResourceManager manager) {
				if (!clientResources) {
					throw new AssertionError("Second reload listener was called before the first!");
				}
			}

			@Override
			public Collection<Identifier> getFabricDependencies() {
				return Collections.singletonList(new Identifier(MODID, "client_first"));
			}
		});

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier(MODID, "client_first");
			}

			@Override
			public void reload(ResourceManager manager) {
				clientResources = true;
			}
		});
	}

	private static void setupServerReloadListeners() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier(MODID, "server_second");
			}

			@Override
			public void reload(ResourceManager manager) {
				if (!serverResources) {
					throw new AssertionError("Second reload listener was called before the first!");
				}
			}

			@Override
			public Collection<Identifier> getFabricDependencies() {
				return Collections.singletonList(new Identifier(MODID, "server_first"));
			}
		});

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier(MODID, "server_first");
			}

			@Override
			public void reload(ResourceManager manager) {
				serverResources = true;
			}
		});
	}
}
