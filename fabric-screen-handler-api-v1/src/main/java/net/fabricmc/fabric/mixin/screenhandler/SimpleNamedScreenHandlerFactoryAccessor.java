package net.fabricmc.fabric.mixin.screenhandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;

@Mixin(SimpleNamedScreenHandlerFactory.class)
public interface SimpleNamedScreenHandlerFactoryAccessor {
    @Accessor
	ScreenHandlerFactory getBaseFactory();
}
