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

package net.fabricmc.fabric.api.registry;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A registry for hoe tilling interactions. A vanilla example is turning dirt to dirt paths.
 */
@EventBusSubscriber
public final class TillableBlockRegistry {
	private static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = new IdentityHashMap<>();

	private TillableBlockRegistry() {
	}

	/**
	 * Registers a tilling interaction.
	 *
	 * <p>Tilling interactions are a two-step process. First, a usage predicate is run that decides whether to till
	 * a block. If the predicate returns {@code true}, an action is executed. Default instances of these can be created
	 * with these {@link HoeItem} methods:
	 * <ul>
	 * <li>usage predicate for farmland-like behavior: {@link HoeItem#onlyIfAirAbove(UseOnContext)}</li>
	 * <li>simple action: {@link HoeItem#changeIntoState(BlockState)} (BlockState)}</li>
	 * <li>simple action that also drops an item: {@link HoeItem#changeIntoStateAndDropItem(BlockState, ItemLike)}</li>
	 * </ul>
	 *
	 * @param input          the input block that can be tilled
	 * @param usagePredicate a predicate that filters if the block can be tilled
	 * @param tillingAction  an action that is executed if the predicate returns {@code true}
	 */
	public static void register(Block input, Predicate<UseOnContext> usagePredicate, Consumer<UseOnContext> tillingAction) {
		Objects.requireNonNull(input, "input block cannot be null");
		TILLABLES.put(input, Pair.of(usagePredicate, tillingAction));
	}

	/**
	 * Registers a simple tilling interaction.
	 *
	 * @param input          the input block that can be tilled
	 * @param usagePredicate a predicate that filters if the block can be tilled
	 * @param tilled         the tilled result block state
	 */
	public static void register(Block input, Predicate<UseOnContext> usagePredicate, BlockState tilled) {
		Objects.requireNonNull(tilled, "tilled block state cannot be null");
		register(input, usagePredicate, HoeItem.changeIntoState(tilled));
	}

	/**
	 * Registers a simple tilling interaction that also drops an item.
	 *
	 * @param input          the input block that can be tilled
	 * @param usagePredicate a predicate that filters if the block can be tilled
	 * @param tilled         the tilled result block state
	 * @param droppedItem    an item that is dropped when the input block is tilled
	 */
	public static void register(Block input, Predicate<UseOnContext> usagePredicate, BlockState tilled, ItemLike droppedItem) {
		Objects.requireNonNull(tilled, "tilled block state cannot be null");
		Objects.requireNonNull(droppedItem, "dropped item cannot be null");
		register(input, usagePredicate, HoeItem.changeIntoStateAndDropItem(tilled, droppedItem));
	}

	@SubscribeEvent
	static void modify(BlockEvent.BlockToolModificationEvent event) {
		if (event.getItemAbility() == ItemAbilities.HOE_TILL
				&& event.getHeldItemStack().canPerformAction(ItemAbilities.HOE_TILL)
		) {
			var modified = TILLABLES.get(event.getState().getBlock());
			if (modified != null && modified.getFirst().test(event.getContext())) {
				if (!event.isSimulated() && !event.getLevel().isClientSide()) {
					modified.getSecond().accept(event.getContext());
					if (event.getContext().getPlayer() != null) {
						event.getContext().getItemInHand()
								.hurtAndBreak(
										1,
										event.getPlayer(),
										getSlotForHand(event.getContext().getHand())
								);
					}
				}
				event.setCanceled(true);
			}
		}
	}

	private static EquipmentSlot getSlotForHand(InteractionHand arg) {
		return arg == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
	}
}
