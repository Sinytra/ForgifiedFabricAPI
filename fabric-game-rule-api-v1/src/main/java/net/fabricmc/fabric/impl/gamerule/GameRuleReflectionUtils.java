package net.fabricmc.fabric.impl.gamerule;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.coremod.api.ASMAPI;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

// Provides access to instance fields of anonymous classes via reflection
// as a replacement for Shadow fields due to an ongoing mixin issue https://github.com/SpongePowered/Mixin/issues/560
public final class GameRuleReflectionUtils {
    private static final VarHandle OWN_GAME_RULES_SCREEN;
    private static final VarHandle OWN_ARGUMENT_BUILDER;

    static {
        try {
            Class<?> cls = Class.forName("net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen$RuleList$1");
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(cls, MethodHandles.lookup());
            OWN_GAME_RULES_SCREEN = lookup.findVarHandle(cls, ASMAPI.mapField("f_101213_"), EditGameRulesScreen.class);

            Class<?> gameRuleCommandsCls = Class.forName("net.minecraft.server.commands.GameRuleCommand$1");
            MethodHandles.Lookup gameRuleCommandLookup = MethodHandles.privateLookupIn(gameRuleCommandsCls, MethodHandles.lookup());
            // Can't use ASMAPI.mapField here due to a mappings mismatch - f_137760_ normally maps to val$base which doesn't exist in dev 
            OWN_ARGUMENT_BUILDER = gameRuleCommandLookup.findVarHandle(gameRuleCommandsCls, FMLEnvironment.production ? "f_137760_" : "val$literalargumentbuilder", LiteralArgumentBuilder.class);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static EditGameRulesScreen getGameRulesScreen(Object self) {
        return (EditGameRulesScreen) OWN_GAME_RULES_SCREEN.get(self);
    }

    @SuppressWarnings("unchecked")
    public static LiteralArgumentBuilder<CommandSourceStack> getLiteralArgumentBuilder(Object self) {
        return (LiteralArgumentBuilder<CommandSourceStack>) OWN_ARGUMENT_BUILDER.get(self);
    }

    private GameRuleReflectionUtils() {}
}
