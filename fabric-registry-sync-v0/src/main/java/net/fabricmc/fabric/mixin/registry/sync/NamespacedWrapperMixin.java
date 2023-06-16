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
import net.fabricmc.fabric.impl.registry.sync.ListenableRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraftforge/registries/NamespacedWrapper")
public abstract class NamespacedWrapperMixin<T> {

    @Inject(method = "registerMapping", at = @At("RETURN"))
    public void setPost(int id, ResourceKey<T> registryId, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder<T>> info) {
        ((ListenableRegistry<T>) this).fabric_getAddObjectEvent().invoker().onEntryAdded(id, registryId.location(), object);
    }
}
