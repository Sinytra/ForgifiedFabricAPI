package net.fabricmc.fabric.impl.biome;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.sinytra.fabric.biome_api.generated.GeneratedEntryPoint;

import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;

@Mod(GeneratedEntryPoint.MOD_ID)
public class FabricBiomeApiV1 {
	public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, GeneratedEntryPoint.MOD_ID);
	public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<BiomeModificationImpl.FabricBiomeModifier>> FABRIC_BIOME_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("fabric_biome_modifier", () -> MapCodec.unit(() -> new BiomeModificationImpl.FabricBiomeModifier(BiomeModificationImpl.INSTANCE.getSortedModifiers())));

	public FabricBiomeApiV1(IEventBus bus) {
		BIOME_MODIFIER_SERIALIZERS.register(bus);
	}
}
