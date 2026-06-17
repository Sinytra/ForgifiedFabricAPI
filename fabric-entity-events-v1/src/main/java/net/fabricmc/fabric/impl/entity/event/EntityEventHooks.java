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

package net.fabricmc.fabric.impl.entity.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.fabricmc.fabric.impl.entity.event.effect.MobEffectUtil;

@EventBusSubscriber
public final class EntityEventHooks {

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && !ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(entity, event.getSource(), event.getAmount())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void afterLivingDamage(LivingDamageEvent.Post event) {
        if (!event.getEntity().isDeadOrDying()) {
            ServerLivingEntityEvents.AFTER_DAMAGE.invoker().afterDamage(event.getEntity(), event.getSource(), event.getOriginalDamage(), event.getHealthDamage(), event.getBlockedDamage() > 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(CanPlayerSleepEvent event) {
        Player.BedSleepingProblem failureReason = EntitySleepEvents.ALLOW_SLEEPING.invoker().allowSleep(event.getEntity(), event.getPos());

        if (failureReason != null) {
            event.setProblem(failureReason);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ServerPlayerEvents.COPY_FROM.invoker().copyFromPlayer((ServerPlayer) event.getOriginal(), (ServerPlayer) event.getEntity(), !event.isWasDeath());
    }
	
	@SubscribeEvent
	public static void canApplyEffect(MobEffectEvent.Applicable event) {
		if (!event.getEntity().level().isClientSide()
				&& !ServerMobEffectEvents.ALLOW_ADD.invoker().allowAdd(event.getEffectInstance(), event.getEntity(), MobEffectUtil.getCommandContext())
		) {
			event.setResult(Result.DO_NOT_APPLY);
		}
	}
	
	@SubscribeEvent
	public static void tryRemoveEffect(MobEffectEvent.Remove event) {
		if (!ServerMobEffectEvents.ALLOW_EARLY_REMOVE.invoker().allowEarlyRemove(event.getEffectInstance(), event.getEntity(), MobEffectUtil.getCommandContext())) {
			event.setCanceled(true);
		}
	}

    private EntityEventHooks() {}
}
