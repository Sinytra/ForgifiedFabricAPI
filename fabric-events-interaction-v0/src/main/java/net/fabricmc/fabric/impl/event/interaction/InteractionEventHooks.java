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

package net.fabricmc.fabric.impl.event.interaction;

import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

public final class InteractionEventHooks {

    @SubscribeEvent
    public static void onEntityInteractAt(PlayerInteractEvent.EntityInteractSpecific event) {
        Entity entity = event.getTarget();
        EntityHitResult hitResult = new EntityHitResult(entity, event.getLocalPos().add(entity.getPos()));
        ActionResult result = UseEntityCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand(), entity, hitResult);
        if (result != ActionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		ActionResult result = UseEntityCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand(), event.getTarget(), null);
        if (result != ActionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        PlayerEntity player = event.getEntity();
		ActionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.getWorld(), Hand.MAIN_HAND, event.getTarget(), null);
        if (result != ActionResult.PASS) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getSide() == LogicalSide.CLIENT) {
			ActionResult result = AttackBlockCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace());
            if (result != ActionResult.PASS) {
                // Returning true will spawn particles and trigger the animation of the hand -> only for SUCCESS.
                // TODO TEST
                event.setUseBlock(result == ActionResult.SUCCESS ? Event.Result.ALLOW : Event.Result.DENY);
                event.setUseItem(result == ActionResult.SUCCESS ? Event.Result.ALLOW : Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		ActionResult result = UseBlockCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != ActionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		TypedActionResult<ItemStack> result = UseItemCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand());
        if (result.getResult() != ActionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result.getResult());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        PlayerEntity player = event.getPlayer();
        World level = player.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        BlockEntity be = level.getBlockEntity(pos);
        boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, be);

        if (!result) {
            PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(level, player, pos, state, be);

            event.setCanceled(true);
        }
    }

    private InteractionEventHooks() {}
}
