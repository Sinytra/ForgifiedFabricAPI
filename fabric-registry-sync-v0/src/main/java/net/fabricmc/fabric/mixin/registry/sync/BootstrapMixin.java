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

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Redirect(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/registries/BuiltInRegistries;bootStrap()V"))
    private static void initialize() {
        RegistriesAccessor.callCreateContents();

        // These seemingly pointless accesses are done to make sure each
        // static initializer is called, to register vanilla-provided blocks
        // and items from the respective classes - otherwise, they would
        // duplicate our calls from below.
        Object oBlock = Blocks.AIR;
        Object oFluid = Fluids.EMPTY;
        Object oItem = Items.AIR;

        RegistrySyncManager.bootstrapRegistries();
    }

    @Redirect(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/registries/GameData;vanillaSnapshot()V"))
    private static void skipVanillaSnapshot() {
        // NO OP
    }
}
