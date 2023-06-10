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

package net.fabricmc.fabric.mixin.networking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin makes disconnect reason text scrollable.
 */
@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
	@Shadow
	private int textHeight;

	@Unique
	private int actualReasonHeight;

	@Unique
	private int scroll;

	@Unique
	private int maxScroll;

	private DisconnectedScreenMixin() {
		super(null);
	}

	// Inject to right after reasonHeight is stored, to make sure the back button have correct position.
	@Inject(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/DisconnectedScreen;textHeight:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void init(CallbackInfo ci) {
		actualReasonHeight = textHeight;
		textHeight = Math.min(textHeight, height - 100);
		scroll = 0;
		maxScroll = actualReasonHeight - textHeight;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/MultiLineLabel;renderCentered(Lcom/mojang/blaze3d/vertex/PoseStack;II)I"))
	private int render(MultiLineLabel instance, PoseStack matrixStack, int x, int y) {
		GuiComponent.enableScissor(0, y, width, y + textHeight);
		instance.renderCentered(matrixStack, x, y - scroll);
		GuiComponent.disableScissor();

		// Draw gradient at the top/bottom to indicate that the text is scrollable.
		if (actualReasonHeight > textHeight) {
			int startX = (width - instance.getWidth()) / 2;
			int endX = (width + instance.getWidth()) / 2;

			if (scroll > 0) {
				fillGradient(matrixStack, startX, y, endX, y + 10, 0xFF000000, 0);
			}

			if (scroll < maxScroll) {
				fillGradient(matrixStack, startX, y + textHeight - 10, endX, y + textHeight, 0, 0xFF000000);
			}
		}

		return y + textHeight;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		scroll = Mth.clamp(scroll - (Mth.sign(amount) * minecraft.font.lineHeight * 10), 0, maxScroll);
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
}
