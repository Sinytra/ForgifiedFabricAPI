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
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;

public final class EnumRuleWidget<E extends Enum<E>> extends EditGameRulesScreen.GameRuleEntry {
	private final Button buttonWidget;
	private final String rootTranslationKey;

	public EnumRuleWidget(EditGameRulesScreen gameRuleScreen, Component name, List<FormattedCharSequence> description, final String ruleName, EnumRule<E> rule, String translationKey) {
		gameRuleScreen.super(description, name);

		// Overwrite line wrapping to account for button larger than vanilla's by 44 pixels.
		this.label = Minecraft.getInstance().font.split(name, 175 - 44);

		// Base translation key needs to be set before the button widget is created.
		this.rootTranslationKey = translationKey;
		this.buttonWidget = Button.builder(this.getValueText(rule.get()), (buttonWidget) -> {
			rule.cycle();
			buttonWidget.setMessage(this.getValueText(rule.get()));
		}).pos(10, 5).size(88, 20).build();

		this.children.add(this.buttonWidget);
	}

	public Component getValueText(E value) {
		final String key = this.rootTranslationKey + "." + value.name().toLowerCase(Locale.ROOT);

		if (I18n.exists(key)) {
			return Component.translatable(key);
		}

		return Component.literal(value.toString());
	}

	@Override
	public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		// FIXME: Param names nightmare
		this.renderLabel(matrices, y, x);

		this.buttonWidget.setPosition(x + entryWidth - 89, y);
		this.buttonWidget.render(matrices, mouseX, mouseY, tickDelta);
	}
}
