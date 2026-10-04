package net.fabricmc.fabric.impl.attachment.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import org.sinytra.fabric.data_attachment_api.generated.GeneratedEntryPoint;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.impl.attachment.GlobalAttachmentsImpl;
import net.fabricmc.fabric.impl.attachment.sync.clientbound.ClientboundAttachmentSyncPayload;

@Mod(value = GeneratedEntryPoint.MOD_ID, dist = Dist.CLIENT)
public class AttachmentClientModImpl {
	public AttachmentClientModImpl(IEventBus bus) {
		bus.addListener(RegisterClientPayloadHandlersEvent.class, event -> {
			event.register(ClientboundAttachmentSyncPayload.TYPE, (payload, context) -> {
				GlobalAttachmentsImpl listener = (GlobalAttachmentsImpl) context.player().level().globalAttachments();
				AttachmentSync.receiveSyncedDataAttachments(listener, context.player().registryAccess(), payload.types(), payload.syncPayload());
			});
		});

		NeoForge.EVENT_BUS.addListener(PlayerLoggedInEvent.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				GlobalAttachmentsImpl.syncInitialData(player);
			}
		});
	}
}
