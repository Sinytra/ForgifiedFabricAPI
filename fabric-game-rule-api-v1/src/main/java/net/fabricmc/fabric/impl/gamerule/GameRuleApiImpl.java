package net.fabricmc.fabric.impl.gamerule;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_game_rule_api_v1")
public class GameRuleApiImpl {
    public GameRuleApiImpl() {
    }

    // Called from injected ASM hook, return true to cancel method
    @SuppressWarnings("unused")
    public static <T extends GameRules.Value<T>> boolean onRegisterCommand(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder, GameRules.Key<T> key, GameRules.Type<T> type) {
        // Check if our type is a EnumRuleType
        if (type instanceof EnumRuleType) {
            //noinspection rawtypes,unchecked
            EnumRuleCommand.register(argumentBuilder, (GameRules.Key) key, (EnumRuleType) type);
            return true;
        }
        return false;
    }
}
