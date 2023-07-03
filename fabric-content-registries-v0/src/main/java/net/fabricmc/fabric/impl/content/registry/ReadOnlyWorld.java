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

package net.fabricmc.fabric.impl.content.registry;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;

public final class ReadOnlyWorld extends World {
    private final World wrapped;
    private final Scoreboard scoreboard;

    public ReadOnlyWorld(World level) {
        super(null, null, level.getRegistryManager(), level.getDimensionEntry(), null, level.isClient, false, 0, 1);
        this.wrapped = level;
        scoreboard = new Scoreboard();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return wrapped.getBlockState(pos);
    }

	@Override
	public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		
	}

	@Override
	public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

	}

	@Override
	public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

	}

	@Override
	public String asString() {
		return wrapped.asString();
	}

	@Nullable
	@Override
	public Entity getEntityById(int id) {
		return wrapped.getEntityById(id);
	}

	@Nullable
	@Override
	public MapState getMapState(String id) {
		return null;
	}

	@Override
	public void putMapState(String id, MapState state) {

	}

	@Override
	public int getNextMapId() {
		return 0;
	}

	@Override
	public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {

	}

	@Override
	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@Override
	public RecipeManager getRecipeManager() {
		return wrapped.getRecipeManager();
	}

	@Override
	protected EntityLookup<Entity> getEntityLookup() {
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryableTickScheduler<Block> getBlockTickScheduler() {
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ChunkManager getChunkManager() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {

	}

	@Override
	public void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {

	}

	@Override
	public float getBrightness(Direction direction, boolean shaded) {
		return 0;
	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return wrapped.getPlayers();
	}

	@Override
	public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
		return wrapped.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
	}

	@Override
	public FeatureSet getEnabledFeatures() {
		return wrapped.getEnabledFeatures();
	}
}
