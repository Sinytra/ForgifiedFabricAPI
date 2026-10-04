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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;

import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry.Context;

public final class PictureInPictureRendererRegistryImpl {
	private static final List<PictureInPictureRendererRegistry.Factory> FACTORIES = new ArrayList<>();
	private static boolean frozen;

	private PictureInPictureRendererRegistryImpl() {
	}

	public static void register(PictureInPictureRendererRegistry.Factory factory) {
		if (frozen) {
			throw new IllegalStateException("Too late to register, GuiRenderer has already been initialized.");
		}

		FACTORIES.add(factory);
	}

	// Called after the vanilla PiP renderers are created.
	public static void apply(RegisterPictureInPictureRenderersEvent event) {
		frozen = true;

		for (PictureInPictureRendererRegistry.Factory factory : FACTORIES) {
			PictureInPictureRenderer<?> elementRenderer = factory.createRenderer(new ContextImpl(null));
			event.register((Class) elementRenderer.getRenderStateClass(), () -> {
				Context context = new ContextImpl(Minecraft.getInstance());
				return factory.createRenderer(context);
			});
		}
	}

	public record ContextImpl(Minecraft minecraft) implements PictureInPictureRendererRegistry.Context {
	}
}
