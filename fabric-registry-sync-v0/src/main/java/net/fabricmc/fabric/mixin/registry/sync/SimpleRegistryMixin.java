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

package net.fabricmc.fabric.mixin.registry.sync;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryRemovedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.impl.registry.sync.ListenableRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(MappedRegistry.class)
public abstract class SimpleRegistryMixin<T> implements ListenableRegistry<T> {
    @Shadow
    @Final
    private Map<ResourceLocation, Holder.Reference<T>> byLocation;

    @Shadow
    @Final
    private Object2IntMap<T> toId;

    @Unique
    @Final
    private Event<RegistryEntryAddedCallback<T>> fabric_addObjectEvent;

    @Unique
    @Final
    private Event<RegistryEntryRemovedCallback<T>> fabric_removeObjectEvent;

    @Unique
    @Final
    private Event<RegistryIdRemapCallback<T>> fabric_postRemapEvent;

    // The rest of the registry isn't thread-safe, so this one need not be either.
    @Unique
    private boolean fabric_isObjectNew = false;

    @Inject(method = "<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("RETURN"))
    private void injectConstructor(ResourceKey<? extends Registry<T>> pKey, Lifecycle pRegistryLifecycle, boolean p_251014_, CallbackInfo callbackInfo) {
        fabric_addObjectEvent = EventFactory.createArrayBacked(RegistryEntryAddedCallback.class,
            (callbacks) -> (rawId, id, object) -> {
                for (RegistryEntryAddedCallback<T> callback : callbacks) {
                    callback.onEntryAdded(rawId, id, object);
                }
            }
        );
        fabric_removeObjectEvent = EventFactory.createArrayBacked(RegistryEntryRemovedCallback.class,
            (callbacks) -> (rawId, id, object) -> {
                for (RegistryEntryRemovedCallback<T> callback : callbacks) {
                    callback.onEntryRemoved(rawId, id, object);
                }
            }
        );
        fabric_postRemapEvent = EventFactory.createArrayBacked(RegistryIdRemapCallback.class,
            (callbacks) -> (a) -> {
                for (RegistryIdRemapCallback<T> callback : callbacks) {
                    callback.onRemap(a);
                }
            }
        );
    }

    @Override
    public Event<RegistryEntryAddedCallback<T>> fabric_getAddObjectEvent() {
        return fabric_addObjectEvent;
    }

    @Override
    public Event<RegistryEntryRemovedCallback<T>> fabric_getRemoveObjectEvent() {
        return fabric_removeObjectEvent;
    }

    @Override
    public Event<RegistryIdRemapCallback<T>> fabric_getRemapEvent() {
        return fabric_postRemapEvent;
    }

    @Inject(method = "registerMapping", at = @At("HEAD"))
    public void setPre(int id, ResourceKey<T> registryId, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder<T>> info) {
        int indexedEntriesId = this.toId.getInt(object);

        if (indexedEntriesId >= 0) {
            throw new RuntimeException("Attempted to register object " + object + " twice! (at raw IDs " + indexedEntriesId + " and " + id + " )");
        }

        if (!byLocation.containsKey(registryId.location())) {
            fabric_isObjectNew = true;
        } else {
            Holder.Reference<T> oldObject = byLocation.get(registryId.location());

            if (oldObject != null && oldObject.value() != null && oldObject.value() != object) {
//                int oldId = entryToRawId.getInt(oldObject.value());
//
//                if (oldId != id) {
//                    throw new RuntimeException("Attempted to register ID " + registryId + " at different raw IDs (" + oldId + ", " + id + ")! If you're trying to override an item, use .set(), not .register()!");
//                }
//
//                fabric_removeObjectEvent.invoker().onEntryRemoved(oldId, registryId.getValue(), oldObject.value());
                fabric_isObjectNew = true;
            } else {
                fabric_isObjectNew = false;
            }
        }
    }

    @Inject(method = "registerMapping", at = @At("RETURN"))
    public void setPost(int id, ResourceKey<T> registryId, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder<T>> info) {
        if (fabric_isObjectNew) {
            fabric_addObjectEvent.invoker().onEntryAdded(id, registryId.location(), object);
        }
    }
}
