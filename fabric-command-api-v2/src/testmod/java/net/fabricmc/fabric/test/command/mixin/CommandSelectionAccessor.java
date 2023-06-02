package net.fabricmc.fabric.test.command.mixin;

import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Commands.CommandSelection.class)
public interface CommandSelectionAccessor {
    @Accessor
    boolean getIncludeIntegrated();

    @Accessor
    boolean getIncludeDedicated();
}
