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

package net.fabricmc.fabric.impl.gamerule;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class EnumRuleType<E extends Enum<E>> extends GameRules.Type<EnumRule<E>> {
	private final E[] supportedValues;

	public EnumRuleType(Function<GameRules.Type<EnumRule<E>>, EnumRule<E>> ruleFactory, BiConsumer<MinecraftServer, EnumRule<E>> changeCallback, E[] supportedValues, GameRules.VisitorCaller<EnumRule<E>> acceptor) {
		super(null, ruleFactory, changeCallback, acceptor);
		this.supportedValues = supportedValues;
	}

	public void register(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, GameRules.Key<EnumRule<E>> key) {
		LiteralCommandNode<CommandSourceStack> ruleNode = Commands.literal(key.getId()).build();

		for (E supportedValue : this.supportedValues) {
			ruleNode.addChild(Commands.literal(supportedValue.toString()).executes(context -> EnumRuleCommand.executeAndSetEnum(context, supportedValue, key)).build());
		}

		literalArgumentBuilder.then(ruleNode);
	}

	@Override
	@Deprecated
	public RequiredArgumentBuilder<CommandSourceStack, ?> createArgument(String name) {
		return super.createArgument(name);
	}
}
