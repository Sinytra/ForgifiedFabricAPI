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

package net.fabricmc.fabric.api.resource;

import net.minecraft.resources.ResourceLocation;

/**
 * This class contains default keys for various Minecraft resource reload listeners.
 *
 * @see IdentifiableResourceReloadListener
 */
public final class ResourceReloadListenerKeys {
	// client
	public static final ResourceLocation SOUNDS = new ResourceLocation("minecraft:sounds");
	public static final ResourceLocation FONTS = new ResourceLocation("minecraft:fonts");
	public static final ResourceLocation MODELS = new ResourceLocation("minecraft:models");
	public static final ResourceLocation LANGUAGES = new ResourceLocation("minecraft:languages");
	public static final ResourceLocation TEXTURES = new ResourceLocation("minecraft:textures");

	// server
	public static final ResourceLocation TAGS = new ResourceLocation("minecraft:tags");
	public static final ResourceLocation RECIPES = new ResourceLocation("minecraft:recipes");
	public static final ResourceLocation ADVANCEMENTS = new ResourceLocation("minecraft:advancements");
	public static final ResourceLocation FUNCTIONS = new ResourceLocation("minecraft:functions");
	public static final ResourceLocation LOOT_TABLES = new ResourceLocation("minecraft:loot_tables");

	private ResourceReloadListenerKeys() { }
}
