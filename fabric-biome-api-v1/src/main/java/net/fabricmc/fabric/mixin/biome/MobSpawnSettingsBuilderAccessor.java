package net.fabricmc.fabric.mixin.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MobSpawnSettings.Builder.class)
public interface MobSpawnSettingsBuilderAccessor {
    @Accessor
    Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> getMobSpawnCosts();
}
