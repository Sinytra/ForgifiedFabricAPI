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

package net.fabricmc.fabric.api.datagen.v1.provider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.impl.datagen.ForcedTagEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Implement this class (or one of the inner classes) to generate a tag list.
 *
 * <p>Register your implementation using {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 *
 * <p>When generating tags for modded dynamic registry entries (such as biomes), either the entry
 * must be added to the registry using {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint#buildRegistry(RegistrySetBuilder)}
 * or {@link TagBuilder#addOptionalElement(ResourceLocation)} must be used. Otherwise, the data generator cannot
 * find the entry and crashes.
 *
 * <p>Commonly used implementations of this class are provided:
 *
 * @see BlockTagProvider
 * @see ItemTagProvider
 * @see FluidTagProvider
 * @see EntityTypeTagProvider
 */
public abstract class FabricTagProvider<T> extends TagsProvider<T> {
	/**
	 * Constructs a new {@link FabricTagProvider} with the default computed path.
	 *
	 * <p>Common implementations of this class are provided.
	 *
	 * @param output        the {@link FabricDataOutput} instance
	 * @param registriesFuture      the backing registry for the tag type
	 */
	public FabricTagProvider(FabricDataOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registryKey, registriesFuture);
	}

	/**
	 * Implement this method and then use {@link FabricTagProvider#tag} to get and register new tag builders.
	 */
	protected abstract void addTags(HolderLookup.Provider wrapperLookup);

	/**
	 * Override to enable adding objects to the tag builder directly.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected ResourceKey<T> reverseLookup(T element) {
		Registry registry = BuiltInRegistries.REGISTRY.get((ResourceKey) registryKey);

		if (registry != null) {
			Optional<Holder<T>> key = registry.getResourceKey(element);

			if (key.isPresent()) {
				return (ResourceKey<T>) key.get();
			}
		}

		throw new UnsupportedOperationException("Adding objects is not supported by " + getClass());
	}

	/**
	 * Creates a new instance of {@link FabricTagBuilder} for the given {@link TagKey} tag.
	 *
	 * @param tag The {@link TagKey} tag to create the builder for
	 * @return The {@link FabricTagBuilder} instance
	 */
	@Override
	protected FabricTagBuilder tag(TagKey<T> tag) {
		return new FabricTagBuilder(super.tag(tag));
	}

	/**
	 * Extend this class to create {@link Block} tags in the "/blocks" tag directory.
	 */
	public abstract static class BlockTagProvider extends FabricTagProvider<Block> {
		public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, Registries.BLOCK, registriesFuture);
		}

		@Override
		protected ResourceKey<Block> reverseLookup(Block element) {
			return element.builtInRegistryHolder().key();
		}
	}

	/**
	 * Extend this class to create {@link BlockEntityType} tags in the "/block_entity_type" tag directory.
	 */
	public abstract static class BlockEntityTypeTagProvider extends FabricTagProvider<BlockEntityType<?>> {
		public BlockEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(output, Registries.BLOCK_ENTITY_TYPE, completableFuture);
		}

		@Override
		protected ResourceKey<BlockEntityType<?>> reverseLookup(BlockEntityType<?> element) {
			return element.builtInRegistryHolder().key();
		}
	}

	/**
	 * Extend this class to create {@link Item} tags in the "/items" tag directory.
	 */
	public abstract static class ItemTagProvider extends FabricTagProvider<Item> {
		@Nullable
		private final Function<TagKey<Block>, TagBuilder> blockTagBuilderProvider;

		/**
		 * Construct an {@link ItemTagProvider} tag provider <b>with</b> an associated {@link BlockTagProvider} tag provider.
		 *
		 * @param output The {@link FabricDataOutput} instance
		 */
		public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture, @Nullable FabricTagProvider.BlockTagProvider blockTagProvider) {
			super(output, Registries.ITEM, completableFuture);

			this.blockTagBuilderProvider = blockTagProvider == null ? null : blockTagProvider::getOrCreateRawBuilder;
		}

		/**
		 * Construct an {@link ItemTagProvider} tag provider <b>without</b> an associated {@link BlockTagProvider} tag provider.
		 *
		 * @param output The {@link FabricDataOutput} instance
		 */
		public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			this(output, completableFuture, null);
		}

		/**
		 * Copy the entries from a tag with the {@link Block} type into this item tag.
		 *
		 * <p>The {@link ItemTagProvider} tag provider must be constructed with an associated {@link BlockTagProvider} tag provider to use this method.
		 *
		 * @param blockTag The block tag to copy from.
		 * @param itemTag  The item tag to copy to.
		 */
		public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
			TagBuilder blockTagBuilder = Objects.requireNonNull(this.blockTagBuilderProvider, "Pass Block tag provider via constructor to use copy").apply(blockTag);
			TagBuilder itemTagBuilder = this.getOrCreateRawBuilder(itemTag);
			blockTagBuilder.build().forEach(itemTagBuilder::add);
		}

		@Override
		protected ResourceKey<Item> reverseLookup(Item element) {
			return element.builtInRegistryHolder().key();
		}
	}

	/**
	 * Extend this class to create {@link Fluid} tags in the "/fluids" tag directory.
	 */
	public abstract static class FluidTagProvider extends FabricTagProvider<Fluid> {
		public FluidTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(output, Registries.FLUID, completableFuture);
		}

		@Override
		protected ResourceKey<Fluid> reverseLookup(Fluid element) {
			return element.builtInRegistryHolder().key();
		}
	}

	/**
	 * Extend this class to create {@link Enchantment} tags in the "/enchantments" tag directory.
	 */
	public abstract static class EnchantmentTagProvider extends FabricTagProvider<Enchantment> {
		public EnchantmentTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(output, Registries.ENCHANTMENT, completableFuture);
		}
	}

	/**
	 * Extend this class to create {@link EntityType} tags in the "/entity_types" tag directory.
	 */
	public abstract static class EntityTypeTagProvider extends FabricTagProvider<EntityType<?>> {
		public EntityTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(output, Registries.ENTITY_TYPE, completableFuture);
		}

		@Override
		protected ResourceKey<EntityType<?>> reverseLookup(EntityType<?> element) {
			return element.builtInRegistryHolder().key();
		}
	}

	/**
	 * An extension to {@link TagAppender} that provides additional functionality.
	 */
	public final class FabricTagBuilder extends TagAppender<T> implements FabricProvidedTagBuilder<T> {
		private final TagsProvider.TagAppender<T> parent;

		private FabricTagBuilder(TagAppender<T> parent) {
			super(parent.builder, "fabric");
			this.parent = parent;
		}

		/**
		 * Set the value of the `replace` flag in a Tag.
		 *
		 * <p>When set to true the tag will replace any existing tag entries.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		public FabricTagBuilder setReplace(boolean replace) {
			replace(replace);
			return this;
		}

		/**
		 * Add an element to the tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		public FabricTagBuilder add(T element) {
			add(reverseLookup(element));
			return this;
		}

		/**
		 * Add multiple elements to the tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		@SafeVarargs
		public final FabricTagBuilder add(T... element) {
			Stream.of(element).map(FabricTagProvider.this::reverseLookup).forEach(this::add);
			return this;
		}

		/**
		 * Add an element to the tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 * @see #add(ResourceLocation)
		 */
		@Override
		public FabricTagBuilder add(ResourceKey<T> registryKey) {
			parent.add(registryKey);
			return this;
		}

		/**
		 * Add a single element to the tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		public FabricTagBuilder add(ResourceLocation id) {
			builder.addElement(id);
			return this;
		}

		/**
		 * Add an optional {@link ResourceLocation} to the tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		@Override
		public FabricTagBuilder addOptional(ResourceLocation id) {
			parent.addOptional(id);
			return this;
		}

		/**
		 * Add an optional {@link ResourceKey} to the tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		public FabricTagBuilder addOptional(ResourceKey<? extends T> registryKey) {
			return addOptional(registryKey.location());
		}

		/**
		 * Add another tag to this tag.
		 *
		 * <p><b>Note:</b> any vanilla tags can be added to the builder,
		 * but other tags can only be added if it has a builder registered in the same provider.
		 *
		 * <p>Use {@link #forceAddTag(TagKey)} to force add any tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 * @see BlockTags
		 * @see EntityTypeTags
		 * @see FluidTags
		 * @see GameEventTags
		 * @see ItemTags
		 */
		@Override
		public FabricTagBuilder addTag(TagKey<T> tag) {
			builder.addTag(tag.location());
			return this;
		}

		/**
		 * Add another optional tag to this tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		@Override
		public FabricTagBuilder addOptionalTag(ResourceLocation id) {
			parent.addOptionalTag(id);
			return this;
		}

		/**
		 * Add another optional tag to this tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		public FabricTagBuilder addOptionalTag(TagKey<T> tag) {
			return addOptionalTag(tag.location());
		}

		/**
		 * Add another tag to this tag, ignoring any warning.
		 *
		 * <p><b>Note:</b> only use this method if you sure that the tag will be always available at runtime.
		 * If not, use {@link #addOptionalTag(ResourceLocation)} instead.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		public FabricTagBuilder forceAddTag(TagKey<T> tag) {
			builder.add(new ForcedTagEntry(TagEntry.element(tag.location())));
			return this;
		}

		/**
		 * Add multiple elements to this tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		public FabricTagBuilder add(ResourceLocation... ids) {
			for (ResourceLocation id : ids) {
				add(id);
			}

			return this;
		}

		/**
		 * Add multiple elements to this tag.
		 *
		 * @return the {@link FabricTagBuilder} instance
		 */
		@SafeVarargs
		@Override
		public final FabricTagBuilder add(ResourceKey<T>... registryKeys) {
			for (ResourceKey<T> registryKey : registryKeys) {
				add(registryKey);
			}

			return this;
		}
	}
}
