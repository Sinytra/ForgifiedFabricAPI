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

package net.fabricmc.fabric.impl.client.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ParticleFactoryRegistryImpl implements ParticleFactoryRegistry {
    public static final ParticleFactoryRegistryImpl INSTANCE = new ParticleFactoryRegistryImpl();

    private final Map<ParticleType<?>, ParticleProvider<?>> factories = new IdentityHashMap<>();
    private final Map<ParticleType<?>, PendingParticleFactory<?>> constructors = new IdentityHashMap<>();

    private ParticleFactoryRegistryImpl() {}

    @Override
    public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> factory) {
        factories.put(type, factory);
    }

    @Override
    public <T extends ParticleOptions> void register(ParticleType<T> type, PendingParticleFactory<T> constructor) {
        constructors.put(type, constructor);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        for (Map.Entry<ParticleType<?>, ParticleProvider<?>> entry : factories.entrySet()) {
            ParticleType type = entry.getKey();
            ParticleProvider factory = entry.getValue();

            event.registerSpecial(type, factory);
        }

        for (Map.Entry<ParticleType<?>, PendingParticleFactory<?>> entry : constructors.entrySet()) {
            ParticleType type = entry.getKey();
            PendingParticleFactory constructor = entry.getValue();

            event.registerSpriteSet(type, delegate -> {
                FabricSpriteProvider fabricSpriteProvider = new FabricSpriteProviderImpl(Minecraft.getInstance().particleEngine, delegate);
                return constructor.create(fabricSpriteProvider);
            });
        }
    }
}
