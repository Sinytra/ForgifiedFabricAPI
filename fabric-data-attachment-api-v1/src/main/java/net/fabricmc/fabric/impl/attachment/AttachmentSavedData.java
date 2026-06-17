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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * Backing storage for server-side global and level attachments.
 * Thanks to custom {@link #isDirty()} logic, the file is only written if something needs to be persisted.
 */
public class AttachmentSavedData extends SavedData {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentSavedData.class);
	public static final Identifier ID = Identifier.fromNamespaceAndPath("fabric", "attachments");
	private final MinecraftServer server;

	public AttachmentSavedData(MinecraftServer server) {
		this.server = server;
	}

	public static Codec<AttachmentSavedData> codec(MinecraftServer server) {
		return codec(server, () -> "AttachmentSavedData @ global server attachments");
	}

	private static Codec<AttachmentSavedData> codec(MinecraftServer server, ProblemReporter.PathElement reporterContext) {
		return CompoundTag.CODEC.flatXmap(tag -> {
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(reporterContext, LOGGER)) {
				var data = new AttachmentSavedData(server);
				// Note: Side effect here, keep an eye on this
				((GlobalAttachmentsImpl) data.server.globalAttachments()).doDeserializeAttachments(TagValueInput.create(reporter, data.server.registryAccess(), tag));
				return !reporter.isEmpty()
						? DataResult.error(() -> "Deserialisation error in level attachments: " + reporter.getReport())
						: DataResult.success(data);
			}
		}, data -> {
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(reporterContext, LOGGER)) {
				var tag = TagValueOutput.createWithContext(reporter, data.server.registryAccess());
				((GlobalAttachmentsImpl) data.server.globalAttachments()).serializeAttachments(tag);
				return !reporter.isEmpty()
						? DataResult.error(() -> "Serialisation error in level attachments: " + reporter.getReport())
						: DataResult.success(tag.buildResult());
			}
		});
	}

	@Override
	public boolean isDirty() {
		return true;
	}
}
