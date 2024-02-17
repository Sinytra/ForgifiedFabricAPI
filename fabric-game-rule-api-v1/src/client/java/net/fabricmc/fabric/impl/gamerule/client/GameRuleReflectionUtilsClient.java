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

package net.fabricmc.fabric.impl.gamerule.client;

import cpw.mods.modlauncher.api.INameMappingService;

import net.minecraft.client.gui.screen.world.EditGameRulesScreen;

import net.minecraftforge.coremod.api.ASMAPI;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

// Provides access to instance fields of anonymous classes via reflection
// as a replacement for Shadow fields due to an ongoing mixin issue https://github.com/SpongePowered/Mixin/issues/560
public final class GameRuleReflectionUtilsClient {
    private static final VarHandle OWN_GAME_RULES_SCREEN;

    static {
        try {
            Class<?> cls = Class.forName(ObfuscationReflectionHelper.remapName(INameMappingService.Domain.CLASS, "net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen$RuleList$1"));
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(cls, MethodHandles.lookup());
            OWN_GAME_RULES_SCREEN = lookup.findVarHandle(cls, ASMAPI.mapField("f_101213_"), EditGameRulesScreen.class);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static EditGameRulesScreen getGameRulesScreen(Object self) {
        return (EditGameRulesScreen) OWN_GAME_RULES_SCREEN.get(self);
    }

    private GameRuleReflectionUtilsClient() {}
}
