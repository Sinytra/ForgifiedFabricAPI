/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.attachment;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.storage.ValueInput;

import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;
import net.fabricmc.fabric.impl.attachment.sync.clientbound.ClientboundAttachmentSyncPayload;
import net.fabricmc.fabric.mixin.attachment.AttachmentHolderAccessor;
import net.fabricmc.fabric.mixin.attachment.AttachmentTypeAccessor;

public class GlobalAttachmentsImpl extends AttachmentHolder implements GlobalAttachments {
	@Nullable
	private final MinecraftServer server;

	public GlobalAttachmentsImpl(@Nullable MinecraftServer server) {
		this.server = server;
	}

	public void doDeserializeAttachments(ValueInput input) {
		super.deserializeAttachments(input);
	}

	@Override
	public void syncData(AttachmentType<?> type) {
		if (server != null) {
			// We don't use PlayerLookup.all() because when a player respawns,
			// there is a brief period where said player will not be in the server player list.
			// If a global attachment is set then, the respawning player will never receive the update.
			server.getConnection().getConnections().forEach(connection -> {
				// if packet listener is not ServerGamePacketListenerImpl, then player is not in PLAY phase yet
				// initial sync will handle it
				if (connection.getPacketListener() instanceof ServerGamePacketListenerImpl serverGamePacketListener) {
					AttachmentSyncHandler syncHandler = ((AttachmentTypeAccessor) (Object) type).getSyncHandler();

					if (syncHandler != null && syncHandler.sendToPlayer(this, serverGamePacketListener.player)) {
						syncUpdate(this, type, syncHandler, serverGamePacketListener.player);
					}
				}
			});
		}
	}

	public static void syncInitialData(ServerPlayer player) {
		GlobalAttachmentsImpl impl = (GlobalAttachmentsImpl) player.level().getServer().globalAttachments();
		syncInitialAttachments(impl, player);
	}

	private static <T> void syncUpdate(AttachmentHolder holder, AttachmentType<T> type, AttachmentSyncHandler<T> syncHandler, ServerPlayer player) {
		RegistryAccess registryAccess = player.registryAccess();

		var data = FriendlyByteBufUtil.writeCustomData(buf -> {
			var existingData = holder.getExistingDataOrNull(type);
			if (existingData != null) {
				buf.writeBoolean(true);
				syncHandler.write(buf, holder.getData(type), false);
			} else {
				buf.writeBoolean(false);
			}
		}, registryAccess);

		var packet = new ClientboundAttachmentSyncPayload(List.of(type), data).toVanillaClientbound();
		IAttachmentHolder exposed = ((AttachmentHolderAccessor) holder).invokeGetExposedHolder();
		if (syncHandler.sendToPlayer(exposed, player)) {
			if (player.connection.hasChannel(ClientboundAttachmentSyncPayload.TYPE)) {
				player.connection.send(packet);
			}
		}
	}

	@Nullable
	public static ClientboundAttachmentSyncPayload syncInitialAttachments(AttachmentHolder holder, ServerPlayer to) {
		AttachmentHolderAccessor accessor = (AttachmentHolderAccessor) holder;
		
		if (accessor.getAttachments() == null) {
			return null;
		}
		if (!to.connection.hasChannel(ClientboundAttachmentSyncPayload.TYPE)) {
			return null;
		}
		
		boolean anySyncableAttachment = false;
		for (var attachment : accessor.getAttachments().keySet()) {
			anySyncableAttachment = anySyncableAttachment | ((AttachmentTypeAccessor) (Object) attachment).getSyncHandler() != null;
		}
		if (!anySyncableAttachment) {
			return null;
		}
		List<AttachmentType<?>> syncedTypes = new ArrayList<>();
		var data = FriendlyByteBufUtil.writeCustomData(buf -> {
			for (var entry : accessor.getAttachments().entrySet()) {
				AttachmentType<?> type = entry.getKey();
				@SuppressWarnings("unchecked")
				var syncHandler = (AttachmentSyncHandler<Object>) ((AttachmentTypeAccessor) (Object) type).getSyncHandler();
				if (syncHandler != null) {
					int indexBefore = buf.writerIndex();
					buf.writeBoolean(true);
					int indexBetween = buf.writerIndex();
					syncHandler.write(buf, entry.getValue(), true);
					if (indexBetween < buf.writerIndex()) {
						// Actually wrote something
						syncedTypes.add(type);
					} else {
						buf.writerIndex(indexBefore);
					}
				}
			}
		}, to.registryAccess());
		return new ClientboundAttachmentSyncPayload(syncedTypes, data);
	}
}
