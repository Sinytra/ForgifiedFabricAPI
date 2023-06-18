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

package net.fabricmc.fabric.mixin.resource.conditions;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class DataPackContentsMixin {
    /**
     * Clear the tags captured by {@link DataPackContentsMixin}.
     * This must happen after the resource reload is complete, to ensure that the tags remain available throughout the entire "apply" phase.
     */
    @Inject(
            method = "updateRegistryTags",
            at = @At("HEAD")
    )
    public void hookRefresh(RegistryAccess dynamicRegistryManager, CallbackInfo ci) {
        ResourceConditionsImpl.LOADED_TAGS.remove();
        ResourceConditionsImpl.CURRENT_REGISTRIES.remove();
    }

    @Inject(
            method = "loadResources",
            at = @At("HEAD")
    )
    private static void hookReload(ResourceManager manager, RegistryAccess.Frozen dynamicRegistryManager, FeatureFlagSet enabledFeatures, Commands.CommandSelection environment, int functionPermissionLevel, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        ResourceConditionsImpl.CURRENT_FEATURES.set(enabledFeatures);
        ResourceConditionsImpl.CURRENT_REGISTRIES.set(dynamicRegistryManager);
    }
}
