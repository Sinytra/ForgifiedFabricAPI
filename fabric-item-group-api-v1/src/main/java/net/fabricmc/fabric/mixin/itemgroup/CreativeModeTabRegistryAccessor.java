package net.fabricmc.fabric.mixin.itemgroup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(CreativeModeTabRegistry.class)
public interface CreativeModeTabRegistryAccessor {

    @Invoker(remap = false)
    static void callProcessCreativeModeTab(CreativeModeTab creativeModeTab, ResourceLocation name, List<Object> afterEntries, List<Object> beforeEntries) {
        throw new UnsupportedOperationException();
    }
}
