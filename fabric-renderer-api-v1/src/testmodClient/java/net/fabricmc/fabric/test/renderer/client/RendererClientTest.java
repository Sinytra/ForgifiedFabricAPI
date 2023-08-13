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

package net.fabricmc.fabric.test.renderer.client;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.test.renderer.FrameBlock;
import net.fabricmc.fabric.test.renderer.Registration;
import net.minecraft.client.render.RenderLayer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class RendererClientTest {

    public static void onInitializeClient() {
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.resolveModel().register(new ModelResolverImpl());
        });
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        for (FrameBlock frameBlock : Registration.getFrameBlocks()) {
            // We don't specify a material for the frame mesh,
            // so it will use the default material, i.e. the one from BlockRenderLayerMap.
            BlockRenderLayerMap.INSTANCE.putBlock(frameBlock, RenderLayer.getCutoutMipped());
        }
    }
}
