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

package net.fabricmc.fabric.impl.resource.loader;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class ResourceManagerHelperImpl implements ResourceManagerHelper {
    private static final Map<PackType, ResourceManagerHelperImpl> registryMap = new HashMap<>();
    private static final Set<Pair<Component, ModNioResourcePack>> builtinResourcePacks = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagerHelperImpl.class);

    private final Set<ResourceLocation> addedListenerIds = new HashSet<>();
    private final Set<IdentifiableResourceReloadListener> addedListeners = new LinkedHashSet<>();

    public static ResourceManagerHelperImpl get(PackType type) {
        return registryMap.computeIfAbsent(type, t -> new ResourceManagerHelperImpl());
    }

    /**
     * Registers a built-in resource pack. Internal implementation.
     *
     * @param id             the identifier of the resource pack
     * @param subPath        the sub path in the mod resources
     * @param container      the mod container
     * @param displayName    the display name of the resource pack
     * @param activationType the activation type of the resource pack
     * @return {@code true} if successfully registered the resource pack, else {@code false}
     * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, ModContainer, Component, ResourcePackActivationType)
     * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, ModContainer, ResourcePackActivationType)
     */
    public static boolean registerBuiltinResourcePack(ResourceLocation id, String subPath, ModContainer container, Component displayName, ResourcePackActivationType activationType) {
        return registerBuiltinResourcePack(id, subPath, Either.left(container), displayName, activationType);
    }

    /**
     * FFAPI: Support FML Mod Metadata.
     * Registers a built-in resource pack. Internal implementation.
     *
     * @param id             the identifier of the resource pack
     * @param subPath        the sub path in the mod resources
     * @param container      the mod container
     * @param displayName    the display name of the resource pack
     * @param activationType the activation type of the resource pack
     * @return {@code true} if successfully registered the resource pack, else {@code false}
     * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, IModInfo, Component, ResourcePackActivationType)
     * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, IModInfo, ResourcePackActivationType)
     */
    public static boolean registerBuiltinResourcePack(ResourceLocation id, String subPath, IModInfo container, Component displayName, ResourcePackActivationType activationType) {
        return registerBuiltinResourcePack(id, subPath, Either.right(container), displayName, activationType);
    }

    private static boolean registerBuiltinResourcePack(ResourceLocation id, String subPath, Either<ModContainer, IModInfo> container, Component displayName, ResourcePackActivationType activationType) {
        // Assuming the mod has multiple paths, we simply "hope" that the  file separator is *not* different across them
        List<Path> paths = container.map(ModContainer::getRootPaths, modInfo -> List.of(modInfo.getOwningFile().getFile().findResource(".")));
        String separator = paths.get(0).getFileSystem().getSeparator();
        subPath = subPath.replace("/", separator);
        ModNioResourcePack resourcePack = ModNioResourcePack.create(id.toString(), container, subPath, PackType.CLIENT_RESOURCES, activationType);
        ModNioResourcePack dataPack = ModNioResourcePack.create(id.toString(), container, subPath, PackType.SERVER_DATA, activationType);
        if (resourcePack == null && dataPack == null) return false;

        if (resourcePack != null) {
            builtinResourcePacks.add(new Pair<>(displayName, resourcePack));
        }

        if (dataPack != null) {
            builtinResourcePacks.add(new Pair<>(displayName, dataPack));
        }

        return true;
    }

    /**
     * Registers a built-in resource pack. Internal implementation.
     *
     * @param id             the identifier of the resource pack
     * @param subPath        the sub path in the mod resources
     * @param container      the mod container
     * @param activationType the activation type of the resource pack
     * @return {@code true} if successfully registered the resource pack, else {@code false}
     * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, ModContainer, ResourcePackActivationType)
     * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, ModContainer, Component, ResourcePackActivationType)
     */
    public static boolean registerBuiltinResourcePack(ResourceLocation id, String subPath, ModContainer container, ResourcePackActivationType activationType) {
        return registerBuiltinResourcePack(id, subPath, container, Component.literal(id.getNamespace() + "/" + id.getPath()), activationType);
    }

    /**
     * FFAPI: Support FML Mod Metadata.
     * Registers a built-in resource pack. Internal implementation.
     *
     * @param id             the identifier of the resource pack
     * @param subPath        the sub path in the mod resources
     * @param container      the mod container
     * @param activationType the activation type of the resource pack
     * @return {@code true} if successfully registered the resource pack, else {@code false}
     * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, IModInfo, ResourcePackActivationType)
     * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, IModInfo, Component, ResourcePackActivationType)
     */
    public static boolean registerBuiltinResourcePack(ResourceLocation id, String subPath, IModInfo container, ResourcePackActivationType activationType) {
        return registerBuiltinResourcePack(id, subPath, Either.right(container), Component.literal(id.getNamespace() + "/" + id.getPath()), activationType);
    }

    public static void registerBuiltinResourcePacks(PackType resourceType, Consumer<Pack> consumer) {
        // Loop through each registered built-in resource packs and add them if valid.
        for (Pair<Component, ModNioResourcePack> entry : builtinResourcePacks) {
            ModNioResourcePack pack = entry.getSecond();

            // Add the built-in pack only if namespaces for the specified resource type are present.
            if (!pack.getNamespaces(resourceType).isEmpty()) {
                // Make the resource pack profile for built-in pack, should never be always enabled.
                Pack profile = Pack.readMetaAndCreate(
                        entry.getSecond().packId(),
                        entry.getFirst(),
                        pack.getActivationType() == ResourcePackActivationType.ALWAYS_ENABLED,
                        ignored -> entry.getSecond(),
                        resourceType,
                        Pack.Position.TOP,
                        new BuiltinModResourcePackSource(pack.getModName(), pack.getActivationType().isEnabledByDefault())
                );
                consumer.accept(profile);
            }
        }
    }

    public static List<PreparableReloadListener> sort(PackType type, List<PreparableReloadListener> listeners) {
        if (type == null) {
            return listeners;
        }

        ResourceManagerHelperImpl instance = get(type);

        if (instance != null) {
            List<PreparableReloadListener> mutable = new ArrayList<>(listeners);
            instance.sort(mutable);
            return Collections.unmodifiableList(mutable);
        }

        return listeners;
    }

    protected void sort(List<PreparableReloadListener> listeners) {
        listeners.removeAll(addedListeners);

        // General rules:
        // - We *do not* touch the ordering of vanilla listeners. Ever.
        //   While dependency values are provided where possible, we cannot
        //   trust them 100%. Only code doesn't lie.
        // - We addReloadListener all custom listeners after vanilla listeners. Same reasons.

        List<IdentifiableResourceReloadListener> listenersToAdd = Lists.newArrayList(addedListeners);
        Set<ResourceLocation> resolvedIds = new HashSet<>();

        for (PreparableReloadListener listener : listeners) {
            if (listener instanceof IdentifiableResourceReloadListener) {
                resolvedIds.add(((IdentifiableResourceReloadListener) listener).getFabricId());
            }
        }

        int lastSize = -1;

        while (listeners.size() != lastSize) {
            lastSize = listeners.size();

            Iterator<IdentifiableResourceReloadListener> it = listenersToAdd.iterator();

            while (it.hasNext()) {
                IdentifiableResourceReloadListener listener = it.next();

                if (resolvedIds.containsAll(listener.getFabricDependencies())) {
                    resolvedIds.add(listener.getFabricId());
                    listeners.add(listener);
                    it.remove();
                }
            }
        }

        for (IdentifiableResourceReloadListener listener : listenersToAdd) {
            LOGGER.warn("Could not resolve dependencies for listener: " + listener.getFabricId() + "!");
        }
    }

    @Override
    public void registerReloadListener(IdentifiableResourceReloadListener listener) {
        if (!addedListenerIds.add(listener.getFabricId())) {
            LOGGER.warn("Tried to register resource reload listener " + listener.getFabricId() + " twice!");
            return;
        }

        if (!addedListeners.add(listener)) {
            throw new RuntimeException("Listener with previously unknown ID " + listener.getFabricId() + " already in listener set!");
        }
    }

    static void onClientResourcesReload(RegisterClientReloadListenersEvent event) {
        List<PreparableReloadListener> listeners = sort(PackType.CLIENT_RESOURCES, List.of());
        listeners.forEach(event::registerReloadListener);
    }

    static void onServerDataReload(AddReloadListenerEvent event) {
        List<PreparableReloadListener> listeners = sort(PackType.SERVER_DATA, event.getListeners());
        listeners.forEach(event::addListener);
    }
}
