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

package net.fabricmc.fabric.test.entity.event;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(EntityEventTests.MODID)
public final class EntityEventTests {
	public static final String MODID = "fabric_entity_events_v1_testmod";
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityEventTests.class);

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

	public static final RegistryObject<Block> TEST_BED = BLOCKS.register("test_bed", () -> new TestBedBlock(BlockBehaviour.Properties.of(Material.WOOL).strength(1, 1)));
	public static final RegistryObject<Item> TEST_BED_ITEM = ITEMS.register("test_bed", () -> new BlockItem(TEST_BED.get(), new Item.Properties()));
	public static final RegistryObject<Item> DIAMOND_ELYTRA = ITEMS.register("diamond_elytra", DiamondElytraItem::new);

	public EntityEventTests() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		BLOCKS.register(bus);
		ITEMS.register(bus);

		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killed) -> {
			LOGGER.info("Entity Killed: {}", killed);
		});

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			LOGGER.info("Moved player {}: [{} -> {}]", player, origin.dimension().location(), destination.dimension().location());
		});

		ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> {
			LOGGER.info("Moved entity {} -> {}: [({} -> {}]", originalEntity, newEntity, origin.dimension().location(), destination.dimension().location());
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			LOGGER.info("Copied data for {} from {} to {}", oldPlayer.getGameProfile().getName(), oldPlayer, newPlayer);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			LOGGER.info("Respawned {}, [{}, {}]", oldPlayer.getGameProfile().getName(), oldPlayer.getLevel().dimension().location(), newPlayer.getLevel().dimension().location());
		});

		// No fall damage if holding a feather in the main hand
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (source.typeHolder().is(DamageTypes.FALL) && entity.getItemInHand(InteractionHand.MAIN_HAND).is(Items.FEATHER)) {
				LOGGER.info("Avoided {} of fall damage by holding a feather", amount);
				return false;
			}

			return true;
		});

		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			LOGGER.info("{} is going to die to {} damage from {} damage source", entity.getName().getString(), amount, source.getMsgId());

			if (entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.CARROT) {
				entity.setHealth(3.0f);
				return false;
			}

			return true;
		});

		// Test that the legacy event still works
		ServerPlayerEvents.ALLOW_DEATH.register((player, source, amount) -> {
			if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.APPLE) {
				player.setHealth(3.0f);
				return false;
			}

			return true;
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			LOGGER.info("{} died due to {} damage source", entity.getName().getString(), source.getMsgId());
		});

		EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
			// Can't sleep if holds blue wool
			if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BLUE_WOOL)) {
				return Player.BedSleepingProblem.OTHER_PROBLEM;
			}

			return null;
		});

		EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
			LOGGER.info("Entity {} sleeping at {}", entity, sleepingPos);
			BlockState bedState = entity.level.getBlockState(sleepingPos);

			if (bedState.is(TEST_BED.get())) {
				boolean shouldBeOccupied = !entity.getItemInHand(InteractionHand.MAIN_HAND).is(Items.ORANGE_WOOL);

				if (bedState.getValue(TestBedBlock.OCCUPIED) != shouldBeOccupied) {
					throw new AssertionError("Test bed should " + (!shouldBeOccupied ? "not " : "") + "be occupied");
				}
			}
		});

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			LOGGER.info("Entity {} woke up at {}", entity, sleepingPos);
		});

		EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) -> {
			return state.is(TEST_BED.get()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		});

		EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, sleepingPos, sleepingDirection) -> {
			return entity.level.getBlockState(sleepingPos).is(TEST_BED.get()) ? Direction.NORTH : sleepingDirection;
		});

		EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPos, vanillaResult) -> {
			// Yellow wool allows to sleep during the day
			if (player.level.isDay() && player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.YELLOW_WOOL)) {
				return InteractionResult.SUCCESS;
			}

			return InteractionResult.PASS;
		});

		EntitySleepEvents.ALLOW_NEARBY_MONSTERS.register((player, sleepingPos, vanillaResult) -> {
			// Green wool allows monsters and red wool always "detects" monsters
			ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

			if (stack.is(Items.GREEN_WOOL)) {
				return InteractionResult.SUCCESS;
			} else if (stack.is(Items.RED_WOOL)) {
				return InteractionResult.FAIL;
			}

			return InteractionResult.PASS;
		});

		EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, sleepingPos) -> {
			// Don't set spawn if holding white wool
			return !player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WHITE_WOOL);
		});

		EntitySleepEvents.ALLOW_RESETTING_TIME.register(player -> {
			// Don't allow resetting time if holding black wool
			return !player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BLACK_WOOL);
		});

		EntitySleepEvents.SET_BED_OCCUPATION_STATE.register((entity, sleepingPos, bedState, occupied) -> {
			// Don't set occupied state if holding orange wool
			return entity.getItemInHand(InteractionHand.MAIN_HAND).is(Items.ORANGE_WOOL);
		});

		EntitySleepEvents.MODIFY_WAKE_UP_POSITION.register((entity, sleepingPos, bedState, wakeUpPos) -> {
			// If holding cyan wool, wake up 10 blocks above the bed
			if (entity.getItemInHand(InteractionHand.MAIN_HAND).is(Items.CYAN_WOOL)) {
				return Vec3.atCenterOf(sleepingPos).add(0, 10, 0);
			}

			return wakeUpPos;
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("addsleeptestwools").executes(context -> {
				addSleepWools(context.getSource().getPlayer());
				return 0;
			}));
		});

		// Block elytra flight when holding a torch in the off-hand.
		EntityElytraEvents.ALLOW.register(entity -> {
			return !entity.getOffhandItem().is(Items.TORCH);
		});
	}

	private static void addSleepWools(Player player) {
		Inventory inventory = player.getInventory();
		inventory.placeItemBackInInventory(createNamedItem(Items.BLUE_WOOL, "Can't start sleeping"));
		inventory.placeItemBackInInventory(createNamedItem(Items.YELLOW_WOOL, "Sleep whenever"));
		inventory.placeItemBackInInventory(createNamedItem(Items.GREEN_WOOL, "Allow nearby monsters"));
		inventory.placeItemBackInInventory(createNamedItem(Items.RED_WOOL, "Detect nearby monsters"));
		inventory.placeItemBackInInventory(createNamedItem(Items.WHITE_WOOL, "Don't set spawn"));
		inventory.placeItemBackInInventory(createNamedItem(Items.BLACK_WOOL, "Don't reset time"));
		inventory.placeItemBackInInventory(createNamedItem(Items.ORANGE_WOOL, "Don't set occupied state"));
		inventory.placeItemBackInInventory(createNamedItem(Items.CYAN_WOOL, "Wake up high above"));
	}

	private static ItemStack createNamedItem(Item item, String name) {
		ItemStack stack = new ItemStack(item);
		stack.setHoverName(Component.literal(name));
		return stack;
	}
}
