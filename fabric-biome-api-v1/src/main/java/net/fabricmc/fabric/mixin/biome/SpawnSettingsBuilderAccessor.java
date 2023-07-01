package net.fabricmc.fabric.mixin.biome;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.SpawnSettings;

@Mixin(SpawnSettings.Builder.class)
public interface SpawnSettingsBuilderAccessor {
    @Accessor
    Map<EntityType<?>, SpawnSettings.SpawnDensity> getSpawnCosts();
}
