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

package net.fabricmc.fabric.impl.gamerule.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class DoubleRuleWidget extends EditGameRulesScreen.GameRuleEntry {
	private final EditBox textFieldWidget;

	public DoubleRuleWidget(EditGameRulesScreen gameRuleScreen, Component name, List<FormattedCharSequence> description, final String ruleName, DoubleRule rule) {
		gameRuleScreen.super(description, name);

		this.textFieldWidget = new EditBox(Minecraft.getInstance().font, 10, 5, 42, 20,
				name.copy()
				.append("\n")
				.append(ruleName)
				.append("\n")
		);

		this.textFieldWidget.setValue(Double.toString(rule.get()));
		this.textFieldWidget.setResponder(value -> {
			if (rule.validate(value)) {
				this.textFieldWidget.setTextColor(0xE0E0E0);
				gameRuleScreen.clearInvalid(this);
			} else {
				this.textFieldWidget.setTextColor(0xFF0000);
				gameRuleScreen.markInvalid(this);
			}
		});

		this.children.add(this.textFieldWidget);
	}

	@Override
	public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		// FIXME: Param names nightmare
		this.renderLabel(matrices, y, x);

		this.textFieldWidget.setPosition(x + entryWidth - 44, y);
		this.textFieldWidget.render(matrices, mouseX, mouseY, tickDelta);
	}
}
