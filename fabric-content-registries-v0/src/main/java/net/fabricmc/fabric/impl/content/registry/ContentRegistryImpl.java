package net.fabricmc.fabric.impl.content.registry;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_content_registries_v0")
public class ContentRegistryImpl {

    public ContentRegistryImpl() {
        MinecraftForge.EVENT_BUS.addListener(TillableBlockRegistryImpl::onBlockToolModification);
    }
}
