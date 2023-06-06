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

package net.fabricmc.fabric.mixin.gamerule.client;

import net.fabricmc.fabric.api.gamerule.v1.FabricGameRuleVisitor;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.fabricmc.fabric.impl.gamerule.widget.DoubleRuleWidget;
import net.fabricmc.fabric.impl.gamerule.widget.EnumRuleWidget;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;

@Mixin(targets = "net/minecraft/client/gui/screens/worldselection/EditGameRulesScreen$RuleList$1")
public abstract class RuleListWidgetVisitorMixin implements GameRules.GameRuleTypeVisitor, FabricGameRuleVisitor {
	@Final
	@Shadow(aliases = "f_101213_")
	private EditGameRulesScreen val$this$0;
	@Shadow
	protected abstract <T extends GameRules.Value<T>> void addEntry(GameRules.Key<T> key, EditGameRulesScreen.EntryFactory<T> ruleWidgetFactory);

	@Override
	public void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
		this.addEntry(key, (name, description, ruleName, rule) -> {
			return new DoubleRuleWidget(this.val$this$0, name, description, ruleName, rule);
		});
	}

	@Override
	public <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
		this.addEntry(key, (name, description, ruleName, rule) -> {
			return new EnumRuleWidget<>(this.val$this$0, name, description, ruleName, rule, key.getDescriptionId());
		});
	}

	/**
	 * @reason We need to display an enum rule's default value as translated.
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules$Value;serialize()Ljava/lang/String;"), method = "Lnet/minecraft/client/gui/screens/worldselection/EditGameRulesScreen$RuleList$1;addEntry(Lnet/minecraft/world/level/GameRules$Key;Lnet/minecraft/client/gui/screens/worldselection/EditGameRulesScreen$EntryFactory;)V")
	private <T extends GameRules.Value<T>> String displayProperEnumName(GameRules.Value<T> rule, GameRules.Key<T> key, EditGameRulesScreen.EntryFactory<T> widgetFactory) {
		if (rule instanceof EnumRule) {
			String translationKey = key.getDescriptionId() + "." + ((EnumRule<?>) rule).get().name().toLowerCase(Locale.ROOT);

			if (I18n.exists(translationKey)) {
				return I18n.get(translationKey);
			}

			return ((EnumRule<?>) rule).get().toString();
		}

		return rule.serialize();
	}
}
