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

package net.fabricmc.fabric.impl.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.compat.FluidStorageFluidHandler;
import net.fabricmc.fabric.impl.transfer.compat.FluidStorageFluidHandlerItem;
import net.fabricmc.fabric.impl.transfer.compat.ItemStorageItemHandler;
import net.fabricmc.fabric.impl.transfer.compat.SlottedItemStorageItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Mod(TransferApiImpl.MODID)
public class TransferApiImpl {
    public static final String MODID = "fabric_transfer_api_v1";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final AtomicLong version = new AtomicLong();
    @SuppressWarnings("rawtypes")
    public static final Storage EMPTY_STORAGE = new Storage() {
        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public long insert(Object resource, long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long extract(Object resource, long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public Iterator<StorageView> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public long getVersion() {
            return 0;
        }

        @Override
        public String toString() {
            return "EmptyStorage";
        }
    };

    public static <T> Iterator<T> singletonIterator(T it) {
        return new Iterator<T>() {
            boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public T next() {
                if (!hasNext) {
                    throw new NoSuchElementException();
                }

                hasNext = false;
                return it;
            }
        };
    }

    public static <T> Iterator<StorageView<T>> filterEmptyViews(Iterator<StorageView<T>> iterator) {
        return new Iterator<>() {
            StorageView<T> next;

            {
                findNext();
            }

            private void findNext() {
                while (iterator.hasNext()) {
                    next = iterator.next();

                    if (next.getAmount() > 0 && !next.isResourceBlank()) {
                        return;
                    }
                }

                next = null;
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public StorageView<T> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                StorageView<T> ret = next;
                findNext();
                return ret;
            }
        };
    }

    public static <T> List<SingleSlotStorage<T>> makeListView(SlottedStorage<T> storage) {
        return new AbstractList<>() {
            @Override
            public SingleSlotStorage<T> get(int index) {
                return storage.getSlot(index);
            }

            @Override
            public int size() {
                return storage.getSlotCount();
            }
        };
    }

    public TransferApiImpl() {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, TransferApiImpl::onAttachBlockEntityCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, TransferApiImpl::onAttachItemStackCapabilities);
    }

    private static final Map<Storage<?>, LazyOptional<?>> CAPS = new HashMap<>();
    public static final ThreadLocal<Boolean> COMPUTING_CAPABILITY_LOCK = ThreadLocal.withInitial(() -> false);

    public static void onAttachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity be = event.getObject();
        event.addCapability(new ResourceLocation(MODID, "forge_bridge"), new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (!COMPUTING_CAPABILITY_LOCK.get()) {
                    if (cap == ForgeCapabilities.ITEM_HANDLER) {
                        COMPUTING_CAPABILITY_LOCK.set(true);
                        Storage<ItemVariant> storage = ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), side);
                        COMPUTING_CAPABILITY_LOCK.set(false);
                        if (storage != null) {
                            return CAPS.computeIfAbsent(storage, s -> LazyOptional.of(() -> storage instanceof SlottedStorage<ItemVariant> slotted ? new SlottedItemStorageItemHandler(slotted) : new ItemStorageItemHandler(storage))).cast();
                        }
                    }
                    if (cap == ForgeCapabilities.FLUID_HANDLER) {
                        COMPUTING_CAPABILITY_LOCK.set(true);
                        Storage<FluidVariant> storage = FluidStorage.SIDED.find(be.getLevel(), be.getBlockPos(), side);
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

    public static void onAttachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        event.addCapability(new ResourceLocation(MODID, "forge_bridge"), new ICapabilityProvider() {
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
