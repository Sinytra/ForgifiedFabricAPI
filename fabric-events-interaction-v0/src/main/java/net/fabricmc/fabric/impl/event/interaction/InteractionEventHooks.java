package net.fabricmc.fabric.impl.event.interaction;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent.UsePhase;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

@EventBusSubscriber
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
		InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, event.getTarget(), null);
		if (result != InteractionResult.PASS) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START) {
			InteractionResult result = AttackBlockCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace());
			if (result != InteractionResult.PASS) {
				// Returning true will spawn particles and trigger the animation of the hand -> only for SUCCESS.
				// TODO TEST
				event.setUseBlock(result == InteractionResult.SUCCESS ? TriState.TRUE : TriState.FALSE);
				event.setUseItem(result == InteractionResult.SUCCESS ? TriState.TRUE : TriState.FALSE);
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
		InteractionResult result = UseItemCallback.EVENT.invoker().interact(event.getEntity(), event.getLevel(), event.getHand());
		if (result != InteractionResult.PASS) {
			event.setCanceled(true);
			event.setCancellationResult(result);
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BreakBlockEvent event) {
		Player player = event.getPlayer();
		Level level = player.level();
		if (level.isClientSide()) return;

		BlockPos pos = event.getPos();
		BlockState state = event.getState();
		BlockEntity be = level.getBlockEntity(pos);
		boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, be);

		if (!result) {
			PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(level, player, pos, state, be);

			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
		if (event.getUsePhase() == UsePhase.ITEM_AFTER_BLOCK) {
			InteractionResult result = ItemEvents.USE_ON.invoker().useOn(event.getUseOnContext());
			if (result != null && result != InteractionResult.PASS) {
				event.setCanceled(true);
				event.setCancellationResult(result);
			}
		}
	}

	private InteractionEventHooks() {
	}
}
