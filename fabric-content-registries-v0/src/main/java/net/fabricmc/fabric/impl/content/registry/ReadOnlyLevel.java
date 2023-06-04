package net.fabricmc.fabric.impl.content.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ReadOnlyLevel extends Level {
    private final Level wrapped;
    private final Scoreboard scoreboard;

    public ReadOnlyLevel(Level level) {
        super(null, null, level.registryAccess(), level.dimensionTypeRegistration(), null, level.isClientSide, false, 0, 1);
        this.wrapped = level;
        scoreboard = new Scoreboard();
    }

    @Override
    public BlockState getBlockState(BlockPos pPos) {
        return wrapped.getBlockState(pPos);
    }

    @Override
	public FeatureFlagSet enabledFeatures() {
		return null;
	}

	@Override
    public void close() {
        // NO OP
    }

    @Override
    public long getDayTime() {
        return 0L;
    }

    @Override
    public long getGameTime() {
        return 0L;
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        // NO OP
    }

	@Override
	public void playSeededSound(@Nullable Player pPlayer, double pX, double pY, double pZ, Holder<SoundEvent> pSound, SoundSource pSource, float pVolume, float pPitch, long pSeed) {
		// NO OP
	}

	@Override
    public void playSound(@Nullable Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
        // NO OP
    }

    @Override
    public void playSound(@Nullable Player player, Entity entity, SoundEvent event, SoundSource category, float volume, float pitch) {
        // NO OP
    }

    @Override
    public String gatherChunkSourceStats() {
        return null;
    }

    @Nullable
    @Override
    public Entity getEntity(int id) {
        return null;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(String mapName) {
        return null;
    }

    @Override
    public void setMapData(String mapId, MapItemSavedData data) {
        // NO OP
    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
        // NO OP
    }

    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return null;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return null;
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return null;
    }

    @Override
    public ChunkSource getChunkSource() {
        return null;
    }

    @Override
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {
        // NO OP
    }

    @Override
    public void gameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {
        // NO OP
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return 0;
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return null;
    }

    @Override
    public void gameEvent(GameEvent event, Vec3 ppos, GameEvent.Context context) {
        // NO OP

    }

    @Override
    public void playSeededSound(Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, long seed) {
        // NO OP

    }

	@Override
	public void playSeededSound(@Nullable Player pPlayer, Entity pEntity, Holder<SoundEvent> pSound, SoundSource pCategory, float pVolume, float pPitch, long pSeed) {
		// NO OP
	}
}
