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

package net.fabricmc.fabric.mixin.event.interaction;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    protected ServerLevel level;
    @Final
    @Shadow
    protected ServerPlayer player;

    @Inject(at = @At("HEAD"), method = "handleBlockBreakAction", cancellable = true)
    public void startBlockBreak(BlockPos pos, ServerboundPlayerActionPacket.Action playerAction, Direction direction, int worldHeight, int i, CallbackInfo info) {
        if (playerAction != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return;
        InteractionResult result = AttackBlockCallback.EVENT.invoker().interact(this.player, this.level, InteractionHand.MAIN_HAND, pos, direction);

        if (result != InteractionResult.PASS) {
            // The client might have broken the block on its side, so make sure to let it know.
            this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos));

            if (this.level.getBlockState(pos).hasBlockEntity()) {
                BlockEntity blockEntity = this.level.getBlockEntity(pos);

                if (blockEntity != null) {
                    Packet<ClientGamePacketListener> updatePacket = blockEntity.getUpdatePacket();

                    if (updatePacket != null) {
                        this.player.connection.send(updatePacket);
                    }
                }
            }

            info.cancel();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"), method = "removeBlock", locals = LocalCapture.CAPTURE_FAILHARD)
    private void onBlockBroken(BlockPos pos, boolean canHarvest, CallbackInfoReturnable<Boolean> cir, BlockState state) {
        BlockEntity be = this.level.getBlockEntity(pos);
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(this.level, this.player, pos, state, be);
    }
}
