package net.fabricmc.fabric.impl.event.interaction;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public final class InteractionEventHooks {

    @SubscribeEvent
    public static void onEntityInteractAt(PlayerInteractEvent.EntityInteractSpecific event) {
        Entity entity = event.getTarget();
        EntityHitResult hitResult = new EntityHitResult(entity, event.getLocalPos().add(entity.position()));
        InteractionResult result = UseEntityCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand(), entity, hitResult);
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = UseEntityCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand(), event.getTarget(), null);
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.getLevel(), InteractionHand.MAIN_HAND, event.getTarget(), null);
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getSide() == LogicalSide.CLIENT) {
            InteractionResult result = AttackBlockCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace());
            if (result != InteractionResult.PASS) {
                // Returning true will spawn particles and trigger the animation of the hand -> only for SUCCESS.
                // TODO TEST
                event.setUseBlock(result == InteractionResult.SUCCESS ? Event.Result.ALLOW : Event.Result.DENY);
                event.setUseItem(result == InteractionResult.SUCCESS ? Event.Result.ALLOW : Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = UseBlockCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        InteractionResultHolder<ItemStack> result = UseItemCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand());
        if (result.getResult() != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result.getResult());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = player.getLevel();
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
