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

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuiltinResourcePackTestMod {
    private static final Logger LOGGER = LoggerFactory.getLogger(BuiltinResourcePackTestMod.class);

    public static void onInitialize() {
        IModInfo modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();

        if (!ResourceManagerHelper.registerBuiltinResourcePack(new ResourceLocation(ResourceLoaderTestImpl.MODID, "test"), modInfo, Component.literal("Fabric Resource Loader Test Pack"), ResourcePackActivationType.DEFAULT_ENABLED)) {
            LOGGER.warn("Could not register built-in resource pack with custom name.");
        }

        if (!ResourceManagerHelper.registerBuiltinResourcePack(new ResourceLocation(ResourceLoaderTestImpl.MODID, "test2"), modInfo, ResourcePackActivationType.NORMAL)) {
            LOGGER.warn("Could not register built-in resource pack.");
        }
    }
}
