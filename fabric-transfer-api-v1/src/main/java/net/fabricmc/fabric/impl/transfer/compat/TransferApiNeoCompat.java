package net.fabricmc.fabric.impl.transfer.compat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.sinytra.fabric.transfer_api.generated.GeneratedEntryPoint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

@EventBusSubscriber(modid = GeneratedEntryPoint.MOD_ID)
public class TransferApiNeoCompat {
	private static final Map<Storage<?>, Supplier<?>> CAPS = new HashMap<>();
	/**
	 * This lock has two purposes: avoiding recursive calls between {@link net.minecraft.world.level.Level#getCapability(BlockCapability, BlockPos, Object)}}
	 * and {@link BlockApiLookup#find(net.minecraft.world.level.Level, BlockPos, net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.block.entity.BlockEntity, Object) find} as well as influencing the
	 * behavior of {@code find} if it was called from {@code getCapability}.
	 * <p>
	 * The recursive calls occur because our capabilities providers need to access the block lookup API to check if they
	 * should provide a capability (for Fabric from Neo compat), but the block lookup API needs to query the
	 * capabilities (for Neo from Fabric compat). This lock is set immediately before one API calls the other, which
	 * then disables the call from the other API to the first, breaking the recursion.
	 * <p>
	 * Additionally, this lock is used to conditionally disable some of the block lookup API's fallback providers, if
	 * they got invoked by a capability provider. This is needed because Fabric has fallback providers for many Vanilla
	 * things, but Neo already implements their own compat for those.
	 */
	public static final ThreadLocal<Boolean> COMPUTING_CAPABILITY_LOCK = ThreadLocal.withInitial(() -> false);

	public static final ThreadLocal<Boolean> WAS_ABORTED = ThreadLocal.withInitial(() -> null);

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	private static void onAttachBlockEntityCapabilities(RegisterCapabilitiesEvent event) {
		for (Block type : BuiltInRegistries.BLOCK) {
			event.registerBlock(
					Capabilities.Item.BLOCK,
					(level, pos, state, blockEntity, context) -> {
						if (!COMPUTING_CAPABILITY_LOCK.get() && (blockEntity == null || blockEntity.hasLevel())) {
							COMPUTING_CAPABILITY_LOCK.set(true);
							Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, blockEntity, context);
							COMPUTING_CAPABILITY_LOCK.set(false);

							if (storage != null) {
								Supplier<? extends ResourceHandler<ItemResource>> supplier = (Supplier<? extends ResourceHandler<ItemResource>>)
										CAPS.computeIfAbsent(storage, s ->
												Suppliers.memoize(() ->
														storage instanceof SlottedStorage<ItemVariant> slotted
																? new FabricSlottedItemResourceHandler(slotted)
																: new FabricItemResourceHandler(storage)
												)
										);
								return supplier.get();
							}
						}
						return null;
					},
					type
			);
		}

		for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
			event.registerBlockEntity(
					Capabilities.Fluid.BLOCK,
					type,
					(be, side) -> {
						if (!COMPUTING_CAPABILITY_LOCK.get() && be.hasLevel()) {
							COMPUTING_CAPABILITY_LOCK.set(true);
							Storage<FluidVariant> storage = FluidStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
							COMPUTING_CAPABILITY_LOCK.set(false);

							if (storage != null) {
								Supplier<? extends ResourceHandler<FluidResource>> supplier = (Supplier<? extends ResourceHandler<FluidResource>>)
										CAPS.computeIfAbsent(storage, s ->
												Suppliers.memoize(() ->
														storage instanceof SlottedStorage<FluidVariant> slotted
																? new FabricSlottedFluidResourceHandler(slotted)
																: new FabricFluidResourceHandler(storage)
												)
										);
								return supplier.get();
							}
						}
						return null;
					}
			);
		}

		for (Item item : BuiltInRegistries.ITEM) {
			event.registerItem(
					Capabilities.Item.ITEM,
					(stack, ctx) -> {
						if (!COMPUTING_CAPABILITY_LOCK.get()) {
							COMPUTING_CAPABILITY_LOCK.set(true);
							Storage<ItemVariant> storage = ItemStorage.ITEM.find(stack, new NeoContainerItemContext(ctx));
							COMPUTING_CAPABILITY_LOCK.set(false);

							if (storage != null) {
								Supplier<? extends ResourceHandler<ItemResource>> supplier = (Supplier<? extends ResourceHandler<ItemResource>>)
										CAPS.computeIfAbsent(storage, s ->
												Suppliers.memoize(() ->
														storage instanceof SlottedStorage<ItemVariant> slotted
																? new FabricSlottedItemResourceHandler(slotted)
																: new FabricItemResourceHandler(storage)
												)
										);
								return supplier.get();
							}
						}
						return null;
					},
					item
			);

			event.registerItem(
					Capabilities.Fluid.ITEM,
					(stack, ctx) -> {
						if (!COMPUTING_CAPABILITY_LOCK.get()) {
							COMPUTING_CAPABILITY_LOCK.set(true);
							Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, new NeoContainerItemContext(ctx));
							COMPUTING_CAPABILITY_LOCK.set(false);

							if (storage != null) {
								Supplier<? extends ResourceHandler<FluidResource>> supplier = (Supplier<? extends ResourceHandler<FluidResource>>)
										CAPS.computeIfAbsent(storage, s ->
												Suppliers.memoize(() ->
														storage instanceof SlottedStorage<FluidVariant> slotted
																? new FabricSlottedFluidResourceHandler(slotted)
																: new FabricFluidResourceHandler(storage)
												)
										);
								return supplier.get();
							}
						}
						return null;
					},
					item
			);
		}
	}

	public static void registerTransferApiFluidNeoBridge() {
		FluidStorage.SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
			if (!COMPUTING_CAPABILITY_LOCK.get()) {
				COMPUTING_CAPABILITY_LOCK.set(true);
				Storage<FluidVariant> storage = Optional.ofNullable(level.getCapability(Capabilities.Fluid.BLOCK, pos, state, blockEntity, direction))
						.map(NeoFluidSlottedStorage::new)
						.orElse(null);
				COMPUTING_CAPABILITY_LOCK.set(false);
				return storage;
			}
			return null;
		});
		FluidStorage.ITEM.registerFallback((stack, context) -> {
			if (stack != null && !COMPUTING_CAPABILITY_LOCK.get()) {
				COMPUTING_CAPABILITY_LOCK.set(true);
				Storage<FluidVariant> storage = Optional.ofNullable(stack.getCapability(Capabilities.Fluid.ITEM, new FabricItemAccess(context)))
						.map(NeoFluidSlottedStorage::new)
						.orElse(null);
				COMPUTING_CAPABILITY_LOCK.set(false);
				return storage;
			}
			return null;
		});
	}

	public static void registerTransferApiItemNeoBridge() {
		ItemStorage.SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
			if (!COMPUTING_CAPABILITY_LOCK.get()) {
				COMPUTING_CAPABILITY_LOCK.set(true);
				Storage<ItemVariant> storage = Optional.ofNullable(level.getCapability(Capabilities.Item.BLOCK, pos, state, blockEntity, direction))
						.map(NeoItemSlottedStorage::new)
						.orElse(null);
				COMPUTING_CAPABILITY_LOCK.set(false);
				return storage;
			}
			return null;
		});
		ItemStorage.ITEM.registerFallback((stack, ctx) -> {
			if (!COMPUTING_CAPABILITY_LOCK.get()) {
				COMPUTING_CAPABILITY_LOCK.set(true);
				Storage<ItemVariant> storage = Optional.ofNullable(stack.getCapability(Capabilities.Item.ITEM, new FabricItemAccess(ctx)))
						.map(NeoItemSlottedStorage::new)
						.orElse(null);
				COMPUTING_CAPABILITY_LOCK.set(false);
				return storage;
			}
			return null;
		});
	}

	public static <A, B> ItemApiLookup.ItemApiProvider<A, B> wrapProviderSafely(ItemApiLookup.ItemApiProvider<A, B> provider) {
		return (a, b) -> {
			if (COMPUTING_CAPABILITY_LOCK.get()) {
				return null;
			}
			return provider.find(a, b);
		};
	}

	public static <A, B> BlockApiLookup.BlockApiProvider<A, B> wrapProviderSafely(BlockApiLookup.BlockApiProvider<A, B> provider) {
		return (world, pos, state, blockEntity, direction) -> {
			if (COMPUTING_CAPABILITY_LOCK.get()) {
				return null;
			}
			return provider.find(world, pos, state, blockEntity, direction);
		};
	}

	public static boolean isInNeoTx() {
		return COMPUTING_CAPABILITY_LOCK.get();
	}

	private static boolean definesCustomFluidType(Fluid fluid) {
		try {
			fluid.getFluidType();
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}
}
