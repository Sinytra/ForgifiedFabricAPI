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

package net.fabricmc.fabric.mixin.gamerule;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.impl.gamerule.EnumRuleCommand;
import net.fabricmc.fabric.impl.gamerule.EnumRuleType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/commands/GameRuleCommand$1")
public abstract class GameRuleCommandVisitorMixin {
	@Final
	@Shadow(aliases = "f_137760_")
	LiteralArgumentBuilder<CommandSourceStack> val$literalargumentbuilder;

	@Inject(at = @At("HEAD"), method = "visit(Lnet/minecraft/world/level/GameRules$Key;Lnet/minecraft/world/level/GameRules$Type;)V", cancellable = true)
	private <T extends GameRules.Value<T>> void onRegisterCommand(GameRules.Key<T> key, GameRules.Type<T> type, CallbackInfo ci) {
		// Check if our type is a EnumRuleType
		if (type instanceof EnumRuleType) {
			//noinspection rawtypes,unchecked
			EnumRuleCommand.register(this.val$literalargumentbuilder, (GameRules.Key) key, (EnumRuleType) type);
			ci.cancel();
		}
	}
}
