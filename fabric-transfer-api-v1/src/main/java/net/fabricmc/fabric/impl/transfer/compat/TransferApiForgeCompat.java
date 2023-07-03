package net.fabricmc.fabric.impl.transfer.compat;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

public class TransferApiForgeCompat {
	public static void init() {
		MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, TransferApiForgeCompat::onAttachBlockEntityCapabilities);
		MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, TransferApiForgeCompat::onAttachItemStackCapabilities);
	}

	private static final Map<Storage<?>, LazyOptional<?>> CAPS = new HashMap<>();
	public static final ThreadLocal<Boolean> COMPUTING_CAPABILITY_LOCK = ThreadLocal.withInitial(() -> false);

	private static void onAttachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
		BlockEntity be = event.getObject();
		event.addCapability(new Identifier(TransferApiImpl.MODID, "forge_bridge"), new ICapabilityProvider() {
			@Override
			public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
				if (!COMPUTING_CAPABILITY_LOCK.get()) {
					if (cap == ForgeCapabilities.ITEM_HANDLER) {
						COMPUTING_CAPABILITY_LOCK.set(true);
						Storage<ItemVariant> storage = ItemStorage.SIDED.find(be.getWorld(), be.getPos(), side);
						COMPUTING_CAPABILITY_LOCK.set(false);
						if (storage != null) {
							return CAPS.computeIfAbsent(storage, s -> LazyOptional.of(() -> storage instanceof SlottedStorage<ItemVariant> slotted ? new SlottedItemStorageItemHandler(slotted) : new ItemStorageItemHandler(storage))).cast();
						}
					}
					if (cap == ForgeCapabilities.FLUID_HANDLER) {
						COMPUTING_CAPABILITY_LOCK.set(true);
						Storage<FluidVariant> storage = FluidStorage.SIDED.find(be.getWorld(), be.getPos(), side);
						COMPUTING_CAPABILITY_LOCK.set(false);
						if (storage != null) {
							return CAPS.computeIfAbsent(storage, s -> LazyOptional.of(() -> new FluidStorageFluidHandler(storage))).cast();
						}
					}
				}
				return LazyOptional.empty();
			}
		});
	}

	private static void onAttachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		ItemStack stack = event.getObject();
		event.addCapability(new Identifier(TransferApiImpl.MODID, "forge_bridge"), new ICapabilityProvider() {
			@Override
			public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
				if (!COMPUTING_CAPABILITY_LOCK.get()) {
					if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
						COMPUTING_CAPABILITY_LOCK.set(true);
						// TODO Context
						Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
						COMPUTING_CAPABILITY_LOCK.set(false);
						if (storage != null) {
							return CAPS.computeIfAbsent(storage, b -> LazyOptional.of(() -> new FluidStorageFluidHandlerItem(storage, stack))).cast();
						}
					}
				}
				return LazyOptional.empty();
			}
		});
	}
}
