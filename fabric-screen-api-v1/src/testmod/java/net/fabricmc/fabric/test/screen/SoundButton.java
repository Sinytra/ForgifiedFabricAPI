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

package net.fabricmc.fabric.test.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

class SoundButton extends AbstractButton {
	private static final RandomSource RANDOM = RandomSource.create();

	SoundButton(int x, int y, int width, int height) {
		super(x, y, width, height, Component.nullToEmpty("Sound Button"));
	}

	@Override
	public void onPress() {
		final SoundEvent event = BuiltInRegistries.SOUND_EVENT.getRandom(RANDOM).map(Holder.Reference::value).orElse(SoundEvents.GENERIC_EXPLODE);
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F, 1.0F));
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		
	}
}
