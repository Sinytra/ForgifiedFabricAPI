package net.fabricmc.fabric.impl.client.resource.loader;

import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.fabricmc.fabric.mixin.resource.loader.client.ReloadableResourceManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

import java.util.List;

public class ResourceLoaderClient {
    public static void onClientResourcesReload(RegisterClientReloadListenersEvent event) {
        List<PreparableReloadListener> existingListeners = ((ReloadableResourceManagerAccessor) Minecraft.getInstance().getResourceManager()).getListeners();
        List<PreparableReloadListener> listeners = ResourceManagerHelperImpl.sort(PackType.CLIENT_RESOURCES, existingListeners);
        listeners.forEach(event::registerReloadListener);
    }
}
