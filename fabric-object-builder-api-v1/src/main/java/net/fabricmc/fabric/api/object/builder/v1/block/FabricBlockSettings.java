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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Fabric's version of Block.Settings. Adds additional methods and hooks
 * not found in the original class.
 *
 * <p>Make note that this behaves slightly different from the
 * vanilla counterpart, copying some settings that vanilla does not.
 *
 * <p>To use it, simply replace Block.Settings.of() with
 * FabricBlockSettings.of().
 */
public class FabricBlockSettings extends BlockBehaviour.Properties {
    protected FabricBlockSettings(Material material, MaterialColor color) {
        super(material, color);
    }

    protected FabricBlockSettings(Material material, Function<BlockState, MaterialColor> mapColorProvider) {
        super(material, mapColorProvider);
    }

    protected FabricBlockSettings(BlockBehaviour.Properties settings) {
        super(settings.material, settings.materialColor);

        this.material = settings.material;
        hardness(settings.destroyTime);
        resistance(settings.explosionResistance);
        collidable(settings.hasCollision);
        this.isRandomlyTicking = settings.isRandomlyTicking;
		lightLevel(settings.lightEmission);
		this.materialColor = settings.materialColor;
		sounds(settings.soundType);
		slipperiness(settings.friction);
		velocityMultiplier(settings.speedFactor);
		this.dynamicShape = settings.dynamicShape;
		this.canOcclude = settings.canOcclude;
		this.isAir = settings.isAir;
		this.requiresCorrectToolForDrops = settings.requiresCorrectToolForDrops;
		this.offsetFunction = settings.offsetFunction;
		this.spawnParticlesOnBreak = settings.spawnParticlesOnBreak;
		this.requiredFeatures = settings.requiredFeatures;

        // Not copied in vanilla: field definition order
		jumpVelocityMultiplier(settings.jumpFactor);
		drops(settings.drops);
		allowsSpawning(settings.isValidSpawn);
		solidBlock(settings.isRedstoneConductor);
		suffocates(settings.isSuffocating);
		blockVision(settings.isViewBlocking);
		postProcess(settings.hasPostProcess);
		emissiveLighting(settings.emissiveRendering);
    }

    public static FabricBlockSettings of(Material material) {
        return of(material, material.getColor());
    }

    public static FabricBlockSettings of(Material material, MaterialColor color) {
        return new FabricBlockSettings(material, color);
    }

    public static FabricBlockSettings of(Material material, DyeColor color) {
        return new FabricBlockSettings(material, color.getMaterialColor());
    }

    public static FabricBlockSettings of(Material material, Function<BlockState, MaterialColor> mapColor) {
        return new FabricBlockSettings(material, mapColor);
    }

    public static FabricBlockSettings copyOf(BlockBehaviour block) {
        return new FabricBlockSettings(block.properties);
    }

    public static FabricBlockSettings copyOf(BlockBehaviour.Properties settings) {
        return new FabricBlockSettings(settings);
    }

    public FabricBlockSettings noCollision() {
        super.noCollission();
        return this;
    }

    public FabricBlockSettings nonOpaque() {
        super.noOcclusion();
        return this;
    }

    public FabricBlockSettings slipperiness(float value) {
        super.friction(value);
        return this;
    }

    public FabricBlockSettings velocityMultiplier(float velocityMultiplier) {
        super.speedFactor(velocityMultiplier);
        return this;
    }

    public FabricBlockSettings jumpVelocityMultiplier(float jumpVelocityMultiplier) {
        super.jumpFactor(jumpVelocityMultiplier);
        return this;
    }

    public FabricBlockSettings sounds(SoundType group) {
        super.sound(group);
        return this;
    }

    /**
     * @deprecated Please use {@link FabricBlockSettings#lightLevel(ToIntFunction)}.
     */
    @Deprecated
    public FabricBlockSettings lightLevel(ToIntFunction<BlockState> luminanceFunction) {
        super.lightLevel(luminanceFunction);
        return this;
    }

    public FabricBlockSettings luminance(ToIntFunction<BlockState> levelFunction) {
        return this.lightLevel(levelFunction);
    }

    public FabricBlockSettings strength(float hardness, float resistance) {
        super.strength(hardness, resistance);
        return this;
    }

    public FabricBlockSettings breakInstantly() {
        super.instabreak();
        return this;
    }

    public FabricBlockSettings strength(float strength) {
        super.strength(strength);
        return this;
    }

    public FabricBlockSettings ticksRandomly() {
        super.randomTicks();
        return this;
    }

    public FabricBlockSettings dynamicBounds() {
        super.dynamicShape();
        return this;
    }

    public FabricBlockSettings dropsNothing() {
        super.noLootTable();
        return this;
    }

    @Override
    public FabricBlockSettings dropsLike(Block block) {
        super.dropsLike(block);
        return this;
    }

    @Override
    public FabricBlockSettings air() {
        super.air();
        return this;
    }

    public FabricBlockSettings allowsSpawning(BlockBehaviour.StateArgumentPredicate<EntityType<?>> predicate) {
        super.isValidSpawn(predicate);
        return this;
    }

    public FabricBlockSettings solidBlock(BlockBehaviour.StatePredicate predicate) {
        super.isRedstoneConductor(predicate);
        return this;
    }

    public FabricBlockSettings suffocates(BlockBehaviour.StatePredicate predicate) {
        super.isSuffocating(predicate);
        return this;
    }

    public FabricBlockSettings blockVision(BlockBehaviour.StatePredicate predicate) {
        super.isViewBlocking(predicate);
        return this;
    }

    public FabricBlockSettings postProcess(BlockBehaviour.StatePredicate predicate) {
        super.hasPostProcess(predicate);
        return this;
    }

    public FabricBlockSettings emissiveLighting(BlockBehaviour.StatePredicate predicate) {
        super.emissiveRendering(predicate);
        return this;
    }

    /**
     * Make the block require tool to drop and slows down mining speed if the incorrect tool is used.
     */
    public FabricBlockSettings requiresTool() {
        super.requiresCorrectToolForDrops();
        return this;
    }

    public FabricBlockSettings mapColor(MaterialColor color) {
        super.color(color);
        return this;
    }

    public FabricBlockSettings hardness(float hardness) {
        super.destroyTime(hardness);
        return this;
    }

    public FabricBlockSettings resistance(float resistance) {
        super.explosionResistance(resistance);
        return this;
    }

    public FabricBlockSettings offset(BlockBehaviour.OffsetType offsetType) {
        super.offsetType(offsetType);
        return this;
    }

    public FabricBlockSettings noBlockBreakParticles() {
        super.noParticlesOnBreak();
        return this;
    }

    public FabricBlockSettings requires(FeatureFlag... features) {
        super.requiredFeatures(features);
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

    public FabricBlockSettings luminance(int luminance) {
        this.luminance(ignored -> luminance);
        return this;
    }

    public FabricBlockSettings drops(ResourceLocation dropTableId) {
		this.drops = dropTableId;
        return this;
    }

    /* FABRIC DELEGATE WRAPPERS */
	@Deprecated
	public FabricBlockSettings materialColor(MaterialColor color) {
		return this.mapColor(color);
	}

	/**
	 * @deprecated Please migrate to {@link FabricBlockSettings#mapColor(DyeColor)}
	 */
	@Deprecated
	public FabricBlockSettings materialColor(DyeColor color) {
		return this.mapColor(color);
	}

	public FabricBlockSettings mapColor(DyeColor color) {
		return this.mapColor(color.getMaterialColor());
	}

    public FabricBlockSettings collidable(boolean collidable) {
        this.hasCollision = collidable;
        return this;
    }
}
