package net.fabricmc.fabric.impl.client.resource.loader;

import java.util.List;

import net.minecraft.resource.ReloadableResourceManagerImpl;

import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;

import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;

public class ResourceLoaderClient {
    public static void onClientResourcesReload(RegisterClientReloadListenersEvent event) {
        List<ResourceReloader> existingListeners = ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).reloaders;
        List<ResourceReloader> listeners = ResourceManagerHelperImpl.sort(ResourceType.CLIENT_RESOURCES, existingListeners);
        listeners.forEach(event::registerReloadListener);
    }
}
