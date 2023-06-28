package net.fabricmc.fabric.mixin.itemgroup;

import java.util.List;

import net.minecraftforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

@Mixin(CreativeModeTabRegistry.class)
public interface CreativeModeTabRegistryAccessor {

    @Invoker(remap = false)
    static void callProcessCreativeModeTab(ItemGroup creativeModeTab, Identifier name, List<Object> afterEntries, List<Object> beforeEntries) {
        throw new UnsupportedOperationException();
    }
}
