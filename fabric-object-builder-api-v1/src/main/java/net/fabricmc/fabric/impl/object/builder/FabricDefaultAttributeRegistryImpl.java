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

package net.fabricmc.fabric.impl.object.builder;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;

public final class FabricDefaultAttributeRegistryImpl {
    private static final Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> SUPPLIERS = new HashMap<>();

    public static DefaultAttributeContainer register(EntityType<? extends LivingEntity> entityType, DefaultAttributeContainer supplier) {
        return SUPPLIERS.put(entityType, supplier);
    }

    @SubscribeEvent
    public static void onAttributesCreate(EntityAttributeCreationEvent event) {
        SUPPLIERS.forEach(event::put);
    }

    private FabricDefaultAttributeRegistryImpl() {}
}
