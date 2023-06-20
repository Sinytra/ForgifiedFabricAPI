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

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Consumer;

/**
 * Represents a resource pack provider for mods and built-in mods resource packs.
 */
public class ModResourcePackCreator implements RepositorySource {
    private final PackType type;

    public ModResourcePackCreator(PackType type) {
        this.type = type;
    }

    /**
     * Registers the resource packs.
     *
     * @param onLoad The resource pack profile consumer.
     */
    @Override
    public void loadPacks(Consumer<Pack> onLoad) {
        // Register all built-in resource packs provided by mods.
        ResourceManagerHelperImpl.registerBuiltinResourcePacks(this.type, onLoad);
    }
}
