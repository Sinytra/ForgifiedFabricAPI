package net.fabricmc.fabric.impl.attachment;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.impl.attachment.sync.clientbound.ClientboundAttachmentSyncPayload;

import org.sinytra.fabric.data_attachment_api.generated.GeneratedEntryPoint;

@Mod(GeneratedEntryPoint.MOD_ID)
public class AttachmentModImpl {

    public AttachmentModImpl(IEventBus bus) {
        bus.addListener(RegisterEvent.class, event ->
            event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, AttachmentRegistryImpl::registerNeoTypes));
		
		bus.addListener(RegisterPayloadHandlersEvent.class, event -> {
			PayloadRegistrar registrar = event.registrar("1").optional();

			registrar.playToClient(
					ClientboundAttachmentSyncPayload.TYPE,
					ClientboundAttachmentSyncPayload.STREAM_CODEC
			);
		});
		
		NeoForge.EVENT_BUS.addListener(PlayerLoggedInEvent.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				GlobalAttachmentsImpl.syncInitialData(player);
			}
		});
    }
}
