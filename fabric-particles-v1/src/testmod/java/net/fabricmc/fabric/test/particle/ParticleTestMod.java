package net.fabricmc.fabric.test.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.test.particle.client.ParticleTestModClient;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Test mod code adapted from Luligabi1's Particle API Test Mod
// https://github.com/Luligabi1/ParticleExampleMod/tree/8c80966676e98e8e478988f94e07b9666c01402b
@Mod(ParticleTestMod.MODID)
public class ParticleTestMod {
    public static final String MODID = "fabric_particles_v1_testmod";

    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);

    public static final SimpleParticleType GREEN_FLAME = FabricParticleTypes.simple();
    public static final RegistryObject<SimpleParticleType> GREEN_FLAME_REG = PARTICLE_TYPES.register("green_flame", () -> GREEN_FLAME);

    public ParticleTestMod() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ParticleTestModClient.onInitializeClient();
        }
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        PARTICLE_TYPES.register(bus);
    }
}
