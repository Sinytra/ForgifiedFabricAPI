package net.fabricmc.fabric.test.particle.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.test.particle.ParticleTestMod;
import net.minecraft.client.particle.FlameParticle;

public class ParticleTestModClient {

    public static void onInitializeClient() {
        /* Registers our particle client-side.
         * First argument is our particle's instance, created previously on ExampleMod.
         * Second argument is the particle's factory. The factory controls how the particle behaves.
         * In this example, we'll use FlameParticle's Factory. */
        ParticleFactoryRegistry.getInstance().register(ParticleTestMod.GREEN_FLAME, FlameParticle.Provider::new);
    }
}
