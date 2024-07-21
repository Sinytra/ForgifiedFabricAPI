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

package net.fabricmc.fabric.api.object.builder.v1.block;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.fabricmc.fabric.mixin.object.builder.AbstractBlockAccessor;
import net.fabricmc.fabric.mixin.object.builder.AbstractBlockSettingsAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * @deprecated replace with {@link BlockBehaviour.Properties}
 */
@Deprecated
public class FabricBlockSettings extends BlockBehaviour.Properties {
	protected FabricBlockSettings() {
		super();
	}

	protected FabricBlockSettings(BlockBehaviour.Properties settings) {
		this();
		// Mostly Copied from vanilla's copy method
		// Note: If new methods are added to Block settings, an accessor must be added here
		AbstractBlockSettingsAccessor thisAccessor = (AbstractBlockSettingsAccessor) this;
		AbstractBlockSettingsAccessor otherAccessor = (AbstractBlockSettingsAccessor) settings;

		// Copied in vanilla: sorted by vanilla copy order
		this.hardness(otherAccessor.getDestroyTime());
		this.resistance(otherAccessor.getExplosionResistance());
		this.collidable(otherAccessor.getHasCollision());
		thisAccessor.setIsRandomlyTicking(otherAccessor.getIsRandomlyTicking());
		this.luminance(otherAccessor.getLuminance());
		thisAccessor.setMapColor(otherAccessor.getMapColor());
		this.sounds(otherAccessor.getSoundType());
		this.slipperiness(otherAccessor.getFriction());
		this.velocityMultiplier(otherAccessor.getSpeedFactor());
		thisAccessor.setDynamicShape(otherAccessor.getDynamicShape());
		thisAccessor.setCanOcclude(otherAccessor.getCanOcclude());
		thisAccessor.setIsAir(otherAccessor.getIsAir());
		thisAccessor.setIgnitedByLava(otherAccessor.getIgnitedByLava());
		thisAccessor.setLiquid(otherAccessor.getLiquid());
		thisAccessor.setForceSolidOff(otherAccessor.getForceSolidOff());
		thisAccessor.setForceSolidOn(otherAccessor.getForceSolidOn());
		this.pistonBehavior(otherAccessor.getPushReaction());
		thisAccessor.setRequiresCorrectToolForDrops(otherAccessor.isRequiresCorrectToolForDrops());
		thisAccessor.setOffsetFunction(otherAccessor.getOffsetFunction());
		thisAccessor.setSpawnTerrainParticles(otherAccessor.getSpawnTerrainParticles());
		thisAccessor.setRequiredFeatures(otherAccessor.getRequiredFeatures());
		this.emissiveLighting(otherAccessor.getEmissiveRendering());
		this.instrument(otherAccessor.getInstrument());
		thisAccessor.setReplaceable(otherAccessor.getReplaceable());

		// Vanilla did not copy those fields until 23w45a, which introduced
		// copyShallow method (maintaining the behavior previously used by the copy method)
		// and the copy method that copies those fields as well. copyShallow is now
		// deprecated. To maintain compatibility and since this behavior seems to be the
		// more proper way, this copies all the fields, not just the shallow ones.
		// Fields are added by field definition order.
		this.jumpVelocityMultiplier(otherAccessor.getJumpFactor());
		this.drops(otherAccessor.getDrops());
		this.allowsSpawning(otherAccessor.getIsValidSpawn());
		this.solidBlock(otherAccessor.getIsRedstoneConductor());
		this.suffocates(otherAccessor.getIsSuffocating());
		this.blockVision(otherAccessor.getIsViewBlocking());
		this.postProcess(otherAccessor.getHasPostProcess());
	}

	/**
	 * @deprecated replace with {@link BlockBehaviour.Properties#of()}
	 */
	@Deprecated
	public static FabricBlockSettings create() {
		return new FabricBlockSettings();
	}

	/**
	 * @deprecated replace with {@link BlockBehaviour.Properties#of()}
	 */
	@Deprecated
	public static FabricBlockSettings of() {
		return create();
	}

	/**
	 * @deprecated replace with {@link BlockBehaviour.Properties#ofFullCopy(BlockBehaviour)}
	 */
	@Deprecated
	public static FabricBlockSettings copyOf(BlockBehaviour block) {
		return new FabricBlockSettings(((AbstractBlockAccessor) block).getProperties());
	}

	/**
	 * @deprecated replace with {@link BlockBehaviour.Properties#ofFullCopy(BlockBehaviour)}
	 */
	@Deprecated
	public static FabricBlockSettings copyOf(BlockBehaviour.Properties settings) {
		return new FabricBlockSettings(settings);
	}

	@Deprecated
	public FabricBlockSettings noCollision() {
		super.noCollission();
		return this;
	}

	@Deprecated
	public FabricBlockSettings nonOpaque() {
		super.noOcclusion();
		return this;
	}

	@Deprecated
	public FabricBlockSettings slipperiness(float value) {
		super.friction(value);
		return this;
	}

	@Deprecated
	public FabricBlockSettings velocityMultiplier(float velocityMultiplier) {
		super.speedFactor(velocityMultiplier);
		return this;
	}

	@Deprecated
	public FabricBlockSettings jumpVelocityMultiplier(float jumpVelocityMultiplier) {
		super.jumpFactor(jumpVelocityMultiplier);
		return this;
	}

	@Deprecated
	public FabricBlockSettings sounds(SoundType group) {
		super.sound(group);
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings lightLevel(ToIntFunction<BlockState> levelFunction) {
		return this.luminance(levelFunction);
	}

	@Deprecated
	public FabricBlockSettings luminance(ToIntFunction<BlockState> luminanceFunction) {
		super.lightLevel(luminanceFunction);
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings strength(float hardness, float resistance) {
		super.strength(hardness, resistance);
		return this;
	}

	@Deprecated
	public FabricBlockSettings breakInstantly() {
		super.instabreak();
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings strength(float strength) {
		super.strength(strength);
		return this;
	}

	@Deprecated
	public FabricBlockSettings ticksRandomly() {
		super.randomTicks();
		return this;
	}

	@Deprecated
	public FabricBlockSettings dynamicBounds() {
		super.dynamicShape();
		return this;
	}

	@Deprecated
	public FabricBlockSettings dropsNothing() {
		super.noLootTable();
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings dropsLike(Block block) {
		super.dropsLike(block);
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings air() {
		super.air();
		return this;
	}

	@Deprecated
	public FabricBlockSettings allowsSpawning(BlockBehaviour.StateArgumentPredicate<EntityType<?>> predicate) {
		super.isValidSpawn(predicate);
		return this;
	}

	@Deprecated
	public FabricBlockSettings solidBlock(BlockBehaviour.StatePredicate predicate) {
		super.isRedstoneConductor(predicate);
		return this;
	}

	@Deprecated
	public FabricBlockSettings suffocates(BlockBehaviour.StatePredicate predicate) {
		super.isSuffocating(predicate);
		return this;
	}

	@Deprecated
	public FabricBlockSettings blockVision(BlockBehaviour.StatePredicate predicate) {
		super.isViewBlocking(predicate);
		return this;
	}

	@Deprecated
	public FabricBlockSettings postProcess(BlockBehaviour.StatePredicate predicate) {
		super.hasPostProcess(predicate);
		return this;
	}

	@Deprecated
	public FabricBlockSettings emissiveLighting(BlockBehaviour.StatePredicate predicate) {
		super.emissiveRendering(predicate);
		return this;
	}

	/**
	 * Make the block require tool to drop and slows down mining speed if the incorrect tool is used.
	 */
	@Deprecated
	public FabricBlockSettings requiresTool() {
		super.requiresCorrectToolForDrops();
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings mapColor(MapColor color) {
		super.mapColor(color);
		return this;
	}

	@Deprecated
	public FabricBlockSettings hardness(float hardness) {
		super.destroyTime(hardness);
		return this;
	}

	@Deprecated
	public FabricBlockSettings resistance(float resistance) {
		super.explosionResistance(resistance);
		return this;
	}

	@Deprecated
	public FabricBlockSettings offset(BlockBehaviour.OffsetType offsetType) {
		super.offsetType(offsetType);
		return this;
	}

	@Deprecated
	public FabricBlockSettings noBlockBreakParticles() {
		super.noTerrainParticles();
		return this;
	}

	@Deprecated
	public FabricBlockSettings requires(FeatureFlag... features) {
		super.requiredFeatures(features);
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings mapColor(Function<BlockState, MapColor> mapColorProvider) {
		super.mapColor(mapColorProvider);
		return this;
	}

	@Deprecated
	public FabricBlockSettings burnable() {
		super.ignitedByLava();
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings liquid() {
		super.liquid();
		return this;
	}

	@Deprecated
	public FabricBlockSettings solid() {
		super.forceSolidOn();
		return this;
	}

	@Deprecated
	public FabricBlockSettings notSolid() {
		super.forceSolidOff();
		return this;
	}

	@Deprecated
	public FabricBlockSettings pistonBehavior(PushReaction pistonBehavior) {
		super.pushReaction(pistonBehavior);
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings instrument(NoteBlockInstrument instrument) {
		super.instrument(instrument);
		return this;
	}

	@Deprecated
	@Override
	public FabricBlockSettings replaceable() {
		super.replaceable();
		return this;
	}

	/* FABRIC ADDITIONS*/

	/**
	 * @deprecated Please use {@link FabricBlockSettings#luminance(int)}.
	 */
	@Deprecated
	public FabricBlockSettings lightLevel(int lightLevel) {
		this.luminance(lightLevel);
		return this;
	}

	/**
	 * @deprecated replace with {@link BlockBehaviour.Properties#lightLevel(ToIntFunction)}
	 */
	@Deprecated
	public FabricBlockSettings luminance(int luminance) {
		this.lightLevel(ignored -> luminance);
		return this;
	}

	@Deprecated
	public FabricBlockSettings drops(ResourceKey<LootTable> dropTableId) {
		((AbstractBlockSettingsAccessor) this).setDrops(dropTableId);
		return this;
	}

	/* FABRIC DELEGATE WRAPPERS */

	/**
	 * @deprecated Please migrate to {@link BlockBehaviour.Properties#mapColor(MapColor)}
	 */
	@Deprecated
	public FabricBlockSettings materialColor(MapColor color) {
		return this.mapColor(color);
	}

	/**
	 * @deprecated Please migrate to {@link BlockBehaviour.Properties#mapColor(DyeColor)}
	 */
	@Deprecated
	public FabricBlockSettings materialColor(DyeColor color) {
		return this.mapColor(color);
	}

	/**
	 * @deprecated Please migrate to {@link BlockBehaviour.Properties#mapColor(DyeColor)}
	 */
	@Deprecated
	public FabricBlockSettings mapColor(DyeColor color) {
		return this.mapColor(color.getMapColor());
	}

	@Deprecated
	public FabricBlockSettings collidable(boolean collidable) {
		((AbstractBlockSettingsAccessor) this).setHasCollision(collidable);
		return this;
	}

	@Override
	public FabricBlockSettings lootFrom(Supplier<? extends Block> blockIn) {
		super.lootFrom(blockIn);
		return this;
	}
}
