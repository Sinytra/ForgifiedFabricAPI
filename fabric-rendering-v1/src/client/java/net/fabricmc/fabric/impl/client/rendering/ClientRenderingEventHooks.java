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

import java.io.IOException;
import java.io.UncheckedIOException;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderGuiEvent;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.entity.EntityType;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class ClientRenderingEventHooks {

    static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        ColorProviderRegistryImpl.BLOCK.initialize(event.getBlockColors());
    }

    static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        ColorProviderRegistryImpl.ITEM.initialize(event.getItemColors());
    }

    static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            CoreShaderRegistrationCallback.RegistrationContext context = (id, vertexFormat, loadCallback) -> {
                ShaderProgram program = new ShaderProgram(event.getResourceProvider(), id, vertexFormat);
                event.registerShader(program, loadCallback);
            };
            CoreShaderRegistrationCallback.EVENT.invoker().registerShaders(context);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityRendererRegistryImpl.setup((type, provider) -> event.registerEntityRenderer((EntityType) type, provider));

        BlockEntityRendererRegistryImpl.setup((t, factory) -> event.registerBlockEntityRenderer((BlockEntityType) t, factory));
    }

    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        EntityModelLayerImpl.PROVIDERS.forEach((name, provider) -> event.registerLayerDefinition(name, provider::createModelData));
    }

    static void onPostRenderHud(RenderGuiEvent.Post event) {
        HudRenderCallback.EVENT.invoker().onHudRender(event.getGuiGraphics(), event.getPartialTick());
    }
}
