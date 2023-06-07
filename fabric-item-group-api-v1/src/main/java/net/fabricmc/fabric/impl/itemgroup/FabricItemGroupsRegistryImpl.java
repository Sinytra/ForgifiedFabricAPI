package net.fabricmc.fabric.impl.itemgroup;

import net.fabricmc.fabric.mixin.itemgroup.CreativeModeTabRegistryAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.event.CreativeModeTabEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FabricItemGroupsRegistryImpl {
    private static final Map<ResourceLocation, CreativeModeTab> TABS = new HashMap<>();

    public static void register(ResourceLocation id, CreativeModeTab tab) {
        TABS.put(id, tab);
    }

    static void registerCreativeTabs(CreativeModeTabEvent.Register event) {
        TABS.forEach(FabricItemGroupsRegistryImpl::registerCreativeModeTab);
    }

    private static void registerCreativeModeTab(ResourceLocation name, CreativeModeTab tab) {
        if (CreativeModeTabRegistry.getTab(name) != null)
            throw new IllegalStateException("Duplicate creative mode tab with name: " + name);

        if (tab.isAlignedRight())
            throw new IllegalStateException("CreativeModeTab " + name + " is aligned right, this is not supported!");

        CreativeModeTabRegistryAccessor.callProcessCreativeModeTab(tab, name, List.of(CreativeModeTabs.SPAWN_EGGS), List.of());
    }

    private FabricItemGroupsRegistryImpl() {}
}
