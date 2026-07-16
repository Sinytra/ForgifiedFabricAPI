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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import net.fabricmc.fabric.impl.datagen.ForcedTagEntry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.extensions.ITagAppenderExtension;

/**
 * Interface-injected to {@link TagsProvider.TagAppender}.
 */
@SuppressWarnings("unchecked")
public interface FabricProvidedTagBuilder<T> extends ITagAppenderExtension<T> {
	/**
	 * Sets the value of the {@code replace} flag. When set to {@code true}
	 * this tag will replace contents of any other tag.
	 * @param replace whether to replace the contents of the tag
	 * @return this, for chaining
	 */
	default FabricProvidedTagBuilder<T> setReplace(boolean replace) {
        replace(replace);
        return this;
	}

	/**
	 * Forces a tag key into the tag, bypassing any errors resulting from the
	 * tag not existing at runtime.
	 * @param tag The tag to force into the contents of the tag
	 * @return this, for chaining
	 */
	default FabricProvidedTagBuilder<T> forceAddTag(TagKey<T> tag) {
        self().add(new ForcedTagEntry(TagEntry.element(tag.location())));
        return this;
	}

	/**
     * Removes an entry from the tag.
     *
     * @param element The entry to remove from the contents of the tag
     * @return this, for chaining
     */
	default TagAppender<T> remove(ResourceKey<T> element) {
		return ITagAppenderExtension.super.remove(element);
	}

	/**
	 * Removes multiple entries from the tag.
	 * @param elements The entries to remove from the contents of the tag
	 * @return this, for chaining
	 */
	default FabricProvidedTagBuilder<T> remove(final ResourceKey<T>... elements) {
        removeAll(Arrays.asList(elements));
        return this;
	}

	/**
	 * Removes multiple entries from the tag.
	 * @param elements The entries to remove from the contents of the tag
	 * @return this, for chaining
	 */
	default FabricProvidedTagBuilder<T> removeAll(final Collection<ResourceKey<T>> elements) {
        for (ResourceKey<T> element : elements) {
            remove(element);
        }
        return this;
	}

	/**
	 * Removes multiple entries from the tag.
	 * @param elements The entries to remove from the contents of the tag
	 * @return this, for chaining
	 */
	default FabricProvidedTagBuilder<T> removeAll(final Stream<ResourceKey<T>> elements) {
		elements.forEach(this::remove);
        return this;
	}

	/**
	 * Removes all entries of the specified tag from the tag.
	 * @param tag The tag to remove from the contents of the tag
	 * @return this, for chaining
	 */
	default FabricProvidedTagBuilder<T> removeTag(TagKey<T> tag) {
        remove(tag);
        return this;
	}

    private TagsProvider.TagAppender<T> self() {
        return (TagsProvider.TagAppender<T>) this;
    }
}
