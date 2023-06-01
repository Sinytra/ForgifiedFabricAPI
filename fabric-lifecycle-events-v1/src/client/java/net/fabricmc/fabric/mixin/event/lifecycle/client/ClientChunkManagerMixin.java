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

package net.fabricmc.fabric.mixin.event.lifecycle.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkManagerMixin {
    @Final
    @Shadow
    private ClientLevel level;

    @Inject(method = "replaceWithPacketData", at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/LevelChunk", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onChunkUnload(int x, int z, FriendlyByteBuf buf, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> info, int index, LevelChunk worldChunk, ChunkPos chunkPos) {
        if (worldChunk != null) {
            ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, worldChunk);
        }
    }

    @Inject(
        method = "updateViewRadius",
        at = @At(
            value = "INVOKE",
            target = "net/minecraft/client/multiplayer/ClientChunkCache$Storage.inRange(II)Z"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onUpdateLoadDistance(int loadDistance, CallbackInfo ci, int oldRadius, int newRadius, ClientChunkCache.Storage clientChunkMap, int k, LevelChunk oldChunk, ChunkPos chunkPos) {
        if (!clientChunkMap.inRange(chunkPos.x, chunkPos.z)) {
            ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, oldChunk);
        }
    }
}
