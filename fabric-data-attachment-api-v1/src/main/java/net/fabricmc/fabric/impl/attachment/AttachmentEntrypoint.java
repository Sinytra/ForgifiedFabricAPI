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

import java.util.Map;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.mixin.attachment.AttachmentHolderAccessor;

public class AttachmentEntrypoint implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("fabric-data-attachment-api-v1");

	@Override
	public void onInitialize() {
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
				transfer(oldPlayer, newPlayer, !alive)
		);
		ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL.register((originalEntity, newEntity, origin, destination) ->
				transfer(originalEntity, newEntity, false)
		);
		// using the corresponding player event is unnecessary as no new instance is created
		ServerLivingEntityEvents.MOB_CONVERSION.register((previous, converted, keepEquipment) ->
				transfer(previous, converted, true)
		);
	}

	/**
	 * Copies attachments from the original to the target. This is used when a ProtoChunk is converted to a
	 * LevelChunk, and when an entity is respawned and a new instance is created. For entity respawns, it is
	 * triggered on player respawn, entity conversion, return from the End, or cross-level entity teleportation.
	 * In the first two cases, only the attachments with {@link net.fabricmc.fabric.api.attachment.v1.AttachmentType#copyOnDeath()} will be transferred.
	 */
	@SuppressWarnings("unchecked")
	static void transfer(IAttachmentHolder original, IAttachmentHolder target, boolean isDeath) {
		Map<AttachmentType<?>, ?> attachments = ((AttachmentHolderAccessor) original).invokeGetAttachmentMap();

		if (attachments == null) {
			return;
		}

		for (Map.Entry<AttachmentType<?>, ?> entry : attachments.entrySet()) {
			AttachmentType type = entry.getKey();
			net.fabricmc.fabric.api.attachment.v1.AttachmentType<?> fabricType = AttachmentRegistryImpl.getFabricAttachmentType(type);
			if (fabricType == null) {
				continue;
			}

			if (!isDeath || fabricType.copyOnDeath()) {
				target.setData(type, entry.getValue());
			}
		}
	}
}
