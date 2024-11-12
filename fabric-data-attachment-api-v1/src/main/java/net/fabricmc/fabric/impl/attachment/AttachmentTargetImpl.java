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

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.fabricmc.fabric.impl.attachment.sync.s2c.AttachmentSyncPayloadS2C;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface AttachmentTargetImpl extends AttachmentTarget {
    @Override
    default <A> @Nullable A setAttached(AttachmentType<A> type, @Nullable A value) {
        A ret = AttachmentTarget.super.setAttached(type, value);
        
        if (this.fabric_shouldTryToSync() && type.isSynced()) {
            AttachmentChange change = AttachmentChange.create(fabric_getSyncTargetInfo(), type, value);
            acknowledgeSyncedEntry(type, change);
            this.fabric_syncChange(type, new AttachmentSyncPayloadS2C(List.of(change)));
        }
        
        return ret;
    }

    default void acknowledgeSyncedEntry(AttachmentType<?> type, @Nullable AttachmentChange change) {}

    default AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		// this only makes sense for server objects
		throw new UnsupportedOperationException("Sync target info was not retrieved on server!");
	}

	/*
	 * Computes changes that should be communicated to newcomers (i.e. clients that start tracking this target)
	 */
	default void fabric_computeInitialSyncChanges(ServerPlayer player, Consumer<AttachmentChange> changeOutput) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default void fabric_syncChange(AttachmentType<?> type, AttachmentSyncPayloadS2C payload) {
	}

	default boolean fabric_shouldTryToSync() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
