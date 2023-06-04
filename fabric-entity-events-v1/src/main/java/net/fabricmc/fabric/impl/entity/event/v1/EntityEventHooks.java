package net.fabricmc.fabric.impl.entity.event.v1;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class EntityEventHooks {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level.isClientSide && !ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(entity, event.getSource(), event.getAmount())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSleepingLocationCheck(SleepingLocationCheckEvent event) {
        LivingEntity entity = event.getEntity();
        BlockPos sleepingPos = event.getSleepingLocation();

        BlockState bedState = entity.level.getBlockState(sleepingPos);
        boolean vanillaResult = bedState.getBlock().isBed(bedState, entity.level, sleepingPos, entity);
        InteractionResult result = EntitySleepEvents.ALLOW_BED.invoker().allowBed(entity, sleepingPos, bedState, vanillaResult);

        if (result != InteractionResult.PASS) {
            event.setResult(result.consumesAction() ? Event.Result.ALLOW : Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        Player.BedSleepingProblem failureReason = EntitySleepEvents.ALLOW_SLEEPING.invoker().allowSleep(event.getEntity(), event.getPos());

        if (failureReason != null) {
            event.setResult(failureReason);
        }
    }

    @SubscribeEvent
    public static void onPlayerSleepingTimeCheck(SleepingTimeCheckEvent event) {
        event.getSleepingLocation().ifPresent(sleepingPos -> {
            Player player = event.getEntity();
            InteractionResult result = EntitySleepEvents.ALLOW_SLEEP_TIME.invoker().allowSleepTime(player, sleepingPos, !player.level.isDay());
            if (result != InteractionResult.PASS) {
                event.setResult(result.consumesAction() ? Event.Result.ALLOW : Event.Result.DENY);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ServerPlayerEvents.COPY_FROM.invoker().copyFromPlayer((ServerPlayer) event.getOriginal(), (ServerPlayer) event.getEntity(), !event.isWasDeath());
    }

    private EntityEventHooks() {}
}
