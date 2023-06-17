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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

class StopSoundButton extends AbstractButton {
	private final Screen screen;

	StopSoundButton(Screen screen, int x, int y, int width, int height) {
		super(x, y, width, height, Component.nullToEmpty(""));
		this.screen = screen;
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float tickDelta) {
		// Render the armor icon to test
		RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
		GuiComponent.blit(matrices, this.getX(), this.getY(), this.width, this.height, 43, 27, 9, 9, 256, 256);

		if (this.isMouseOver(mouseX, mouseY)) {
			this.screen.renderTooltip(matrices, Component.literal("Click to stop all sounds"), this.getX(), this.getY());
		}
	}

	@Override
	public void onPress() {
		Minecraft.getInstance().getSoundManager().stop();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		
	}
}
