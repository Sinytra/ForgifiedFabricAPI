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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_rendering_v1")
public class RenderingImpl {

    public RenderingImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(ClientRenderingEventHooks::onRegisterBlockColors);
            bus.addListener(ClientRenderingEventHooks::onRegisterItemColors);
            bus.addListener(ClientRenderingEventHooks::onRegisterShaders);
            bus.addListener(ClientRenderingEventHooks::registerEntityRenderers);
            bus.addListener(ClientRenderingEventHooks::registerLayerDefinitions);

            MinecraftForge.EVENT_BUS.addListener(ClientRenderingEventHooks::onPostRenderHud);
        }
    }
}
