package net.fabricmc.fabric.impl.attachment;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(AttachmentModImpl.MODID)
public class AttachmentModImpl {
    public static final String MODID = "fabric_data_attachment_api_v1";

    public AttachmentModImpl(IEventBus bus) {
        bus.addListener(RegisterEvent.class, event ->
            event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, AttachmentRegistryImpl::registerNeoTypes));
    }
}
