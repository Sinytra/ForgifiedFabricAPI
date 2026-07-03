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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.mixin.attachment.BaseMappedRegistryAccessor;
import net.fabricmc.fabric.mixin.attachment.MappedRegistryAccessor;

public final class AttachmentRegistryImpl {
	private static final Map<net.neoforged.neoforge.attachment.AttachmentType<?>, AttachmentType<?>> FABRIC_ATTACHMENT_TYPES = new ConcurrentHashMap<>();
	private static final Map<Identifier, net.neoforged.neoforge.attachment.AttachmentType<?>> NEO_ATTACHMENT_TYPES = new ConcurrentHashMap<>();
	private static boolean deferRegistration = true;

	public static <A> net.neoforged.neoforge.attachment.AttachmentType<A> registerNeoForgeAttachment(Identifier id, net.neoforged.neoforge.attachment.AttachmentType<A> attachmentType) {
		if (deferRegistration) {
			NEO_ATTACHMENT_TYPES.put(id, attachmentType);
		} else {
			boolean frozen = ((MappedRegistryAccessor) NeoForgeRegistries.ATTACHMENT_TYPES).getFrozen();
			if (frozen) {
				((BaseMappedRegistryAccessor) NeoForgeRegistries.ATTACHMENT_TYPES).invokeUnfreeze(false);
			}
			Registry.register(NeoForgeRegistries.ATTACHMENT_TYPES, id, attachmentType);
			if (frozen) {
				NeoForgeRegistries.ATTACHMENT_TYPES.freeze();
			}
		}
		return attachmentType;
	}

	public static void registerNeoTypes(RegisterEvent.RegisterHelper<net.neoforged.neoforge.attachment.AttachmentType<?>> helper) {
		deferRegistration = false;
		NEO_ATTACHMENT_TYPES.forEach(helper::register);
	}

	@SuppressWarnings("unchecked")
	public static <A> AttachmentType<A> getFabricAttachmentType(net.neoforged.neoforge.attachment.AttachmentType<A> neoType) {
		return (AttachmentType<A>) FABRIC_ATTACHMENT_TYPES.get(neoType);
	}

	public static <A> AttachmentRegistry.Builder<A> builder() {
		return new BuilderImpl<>();
	}

	public static class BuilderImpl<A> implements AttachmentRegistry.Builder<A> {
		@Nullable
		private Supplier<A> defaultInitializer = null;
		@Nullable
		private Codec<A> persistenceCodec = null;
		@Nullable
		private StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec = null;
		@Nullable
		private AttachmentSyncPredicate syncPredicate = null;
		private boolean copyOnDeath = false;
		private int maxSyncSize = -1;

		@Override
		public AttachmentRegistry.Builder<A> persistent(Codec<A> codec) {
			Objects.requireNonNull(codec, "codec cannot be null");

			this.persistenceCodec = codec;
			return this;
		}

		@Override
		public AttachmentRegistry.Builder<A> copyOnDeath() {
			this.copyOnDeath = true;
			return this;
		}

		@Override
		public AttachmentRegistry.Builder<A> initializer(Supplier<A> initializer) {
			Objects.requireNonNull(initializer, "initializer cannot be null");

			this.defaultInitializer = initializer;
			return this;
		}

		@Override
		public AttachmentRegistry.Builder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec, AttachmentSyncPredicate syncPredicate) {
			Objects.requireNonNull(streamCodec, "stream codec cannot be null");
			Objects.requireNonNull(syncPredicate, "sync predicate cannot be null");

			this.streamCodec = streamCodec;
			this.syncPredicate = syncPredicate;
			return this;
		}

		@Override
		public AttachmentRegistry.Builder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec, AttachmentSyncPredicate syncPredicate, int maxSyncSize) {
			if (maxSyncSize < 0) {
				throw new IllegalArgumentException("maxSyncSize must be positive!");
			}

			syncWith(streamCodec, syncPredicate);
			this.maxSyncSize = maxSyncSize;

			return this;
		}

		@Override
		public AttachmentType<A> buildAndRegister(Identifier id) {
			Objects.requireNonNull(id, "identifier cannot be null");

			net.neoforged.neoforge.attachment.AttachmentType<A> neoType = registerNeoForgeAttachment(id, toNeoForgeAttachmentType(id));
			AttachmentType<A> attachmentType = new AttachmentTypeImpl<>(
					neoType,
					id,
					defaultInitializer,
					persistenceCodec,
					streamCodec,
					syncPredicate,
					copyOnDeath,
					maxSyncSize
			);
			FABRIC_ATTACHMENT_TYPES.put(neoType, attachmentType);
			return attachmentType;
		}

		private net.neoforged.neoforge.attachment.AttachmentType<A> toNeoForgeAttachmentType(Identifier id) {
			net.neoforged.neoforge.attachment.AttachmentType.Builder<A> builder = net.neoforged.neoforge.attachment.AttachmentType.builder(this.defaultInitializer != null ? this.defaultInitializer : () -> null);
			if (this.persistenceCodec != null) {
				builder.serialize(this.persistenceCodec.fieldOf(id.getPath()));
				if (this.copyOnDeath) {
					builder.copyOnDeath();
				}
			} else {
				builder.serialize((IAttachmentSerializer) DummyAttachmentSerializer.INSTANCE);
				builder.copyHandler((value, holder, provider) -> value);
			}
			if (this.streamCodec != null) {
				Objects.requireNonNull(this.syncPredicate, "sync predicate cannot be null");
				builder.sync((holder, player) -> this.syncPredicate.test((AttachmentTarget) holder, player), this.streamCodec);
			}
			return builder.build();
		}
	}

	private static class DummyAttachmentSerializer implements IAttachmentSerializer<Object> {
		private static final DummyAttachmentSerializer INSTANCE = new DummyAttachmentSerializer();

		@Override
		public Object read(IAttachmentHolder holder, ValueInput input) {
			return null;
		}

		@Override
		public boolean write(Object attachment, ValueOutput output) {
			return false;
		}
	}
}
