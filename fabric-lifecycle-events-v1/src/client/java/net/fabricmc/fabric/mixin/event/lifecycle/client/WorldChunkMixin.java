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

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(LevelChunk.class)
abstract class WorldChunkMixin {
    @Shadow
    public abstract Level getLevel();

    /*
     * @Inject(method = "setBlockEntity", at = @At(value = "CONSTANT", args = "nullValue=true"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
     *
     * i509VCB: Yes this is very brittle.
     * Sadly mixin does not want to cooperate with the Inject annotation commented out above.
     * Our goal is to place the inject JUST after the possibly removed block entity is stored onto the stack so we can use local capture:
     *
     *  INVOKEVIRTUAL net/minecraft/util/math/BlockPos.toImmutable ()Lnet/minecraft/util/math/BlockPos;
     *  ALOAD 1
     *  INVOKEINTERFACE java/util/Map.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; (itf)
     *  CHECKCAST net/minecraft/block/entity/BlockEntity
     *  ASTORE 3
     *  <======== HERE
     * L6
     */
    @Inject(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BY, by = 3), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onLoadBlockEntity(BlockEntity blockEntity, CallbackInfo ci, BlockPos blockPos, @Nullable BlockEntity removedBlockEntity) {
        // Only fire the load event if the block entity has actually changed
        if (blockEntity != null && blockEntity != removedBlockEntity) {
            if (this.getLevel() instanceof ServerLevel serverLevel) {
                ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(blockEntity, serverLevel);
            } else if (this.getLevel() instanceof ClientLevel clientLevel) {
                ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(blockEntity, clientLevel);
            }
        }
    }

	@Inject(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onRemoveBlockEntity(BlockEntity blockEntity, CallbackInfo info, BlockPos blockPos, @Nullable BlockEntity removedBlockEntity) {
        if (removedBlockEntity != null) {
            if (this.getLevel() instanceof ServerLevel serverLevel) {
                ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removedBlockEntity, serverLevel);
            } else if (this.getLevel() instanceof ClientLevel clientLevel) {
                ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removedBlockEntity, clientLevel);
            }
        }
    }

    // Use the slice to not redirect codepath where block entity is loaded
    @Redirect(
        method = {"getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;"},
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"
        ),
        slice = @Slice(
            to = @At(
                value = "FIELD",
                target = "net/minecraft/world/level/chunk/LevelChunk.pendingBlockEntities:Ljava/util/Map;",
                opcode = Opcodes.GETFIELD
            )
        )
    )
    private <K, V> Object onRemoveBlockEntity(Map<K, V> map, K key) {
        @Nullable final V removed = map.remove(key);

        if (removed != null) {
            if (this.getLevel() instanceof ServerLevel serverLevel) {
                ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload((BlockEntity) removed, serverLevel);
            } else if (this.getLevel() instanceof ClientLevel clientLevel) {
                ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload((BlockEntity) removed, clientLevel);
            }
        }

        return removed;
    }

	@Inject(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci, @Nullable BlockEntity removed) {
        if (removed != null) {
            if (this.getLevel() instanceof ServerLevel serverLevel) {
                ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removed, serverLevel);
            } else if (this.getLevel() instanceof ClientLevel clientLevel) {
                ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removed, clientLevel);
            }
        }
    }
}
