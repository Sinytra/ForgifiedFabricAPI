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

package net.fabricmc.fabric.api.object.builder.v1.block.type;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * This class allows registering {@link WoodType}s.
 *
 * <p>A {@link WoodType} is used to tell the game what textures signs should use, as well as sounds for both signs and fence gates.
 *
 * <p>Regular sign textures are stored at {@code [namespace]/textures/entity/signs/[path].png}.
 * <br>Hanging sign textures are stored at {@code [namespace]/textures/entity/signs/hanging/[path].png}.
 *
 * @see BlockSetTypeRegistry
 */
public final class WoodTypeRegistry {
	private WoodTypeRegistry() {
	}

	/**
	 * Creates and registers a {@link WoodType}.
	 *
	 * @param id the id of this {@link WoodType}
	 * @param setType the {@link BlockSetType} for this wood type
	 * @return a new {@link WoodType}
	 */
	public static WoodType register(ResourceLocation id, BlockSetType setType) {
		return WoodType.register(new WoodType(id.toString(), setType));
	}

	/**
	 * Creates and registers a {@link WoodType}.
	 *
	 * @param id the id of this {@link WoodType}
	 * @param setType the {@link BlockSetType} for this wood type
	 * @param soundType the {@link SoundType} for this wood type
	 * @param hangingSignSoundType the {@link SoundType} for this wood type's hanging sign
	 * @param fenceGateClose the {@link SoundEvent} for when this wood type's fence gate closes
	 * @param fenceGateOpen the {@link SoundEvent} for when this wood type's fence gate opens
	 * @return a new {@link WoodType}
	 */
	public static WoodType register(ResourceLocation id, BlockSetType setType, SoundType soundType, SoundType hangingSignSoundType, SoundEvent fenceGateClose, SoundEvent fenceGateOpen) {
		return WoodType.register(new WoodType(id.toString(), setType, soundType, hangingSignSoundType, fenceGateClose, fenceGateOpen));
	}
}
