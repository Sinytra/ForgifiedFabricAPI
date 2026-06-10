package net.fabricmc.fabric.impl.client.keymapping;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import net.fabricmc.api.ClientModInitializer;

public class FabricKeyMappingImpl implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        IEventBus bus = ModLoadingContext.get().getActiveContainer().getEventBus();
        bus.addListener(RegisterKeyMappingsEvent.class, KeyMappingRegistryImpl::process);
    }
}
