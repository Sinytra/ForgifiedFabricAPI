package net.fabricmc.fabric.impl.biome;

import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BiomeApiImpl.MODID)
public class BiomeApiImpl {
    public static final String MODID = "fabric_biome_api_v1";

    public BiomeApiImpl() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        BiomeModificationImpl.BIOME_MODIFIER_SERIALIZERS.register(bus);
    }
}
