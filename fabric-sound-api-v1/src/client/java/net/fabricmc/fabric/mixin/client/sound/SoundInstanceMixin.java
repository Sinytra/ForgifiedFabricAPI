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

package net.fabricmc.fabric.mixin.client.sound;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;

import net.fabricmc.fabric.api.client.sound.v1.FabricSoundInstance;

import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SoundInstance.class)
public interface SoundInstanceMixin extends FabricSoundInstance {
	// Override the Neo method in SoundInstance
	@Overwrite
	default CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
		return getAudioStream(soundBuffers, sound.getPath(), looping);
	}
}
