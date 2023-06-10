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

package net.fabricmc.fabric.mixin.networking.client;

import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ClientPacketListener.class, priority = 999)
abstract class ClientPlayNetworkHandlerMixin implements NetworkHandlerExtensions {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Unique
    private ClientPlayNetworkAddon addon;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAddon(CallbackInfo ci) {
        this.addon = new ClientPlayNetworkAddon((ClientPacketListener) (Object) this, this.minecraft);
        // A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
        ClientNetworkingImpl.setClientPlayAddon(this.addon);
        this.addon.lateInit();
    }

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void handleServerPlayReady(ClientboundLoginPacket packet, CallbackInfo ci) {
        this.addon.onServerReady();
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (this.addon.handle(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void handleDisconnection(Component reason, CallbackInfo ci) {
        this.addon.handleDisconnect();
    }

    @Override
    public ClientPlayNetworkAddon getAddon() {
        return this.addon;
    }
}
