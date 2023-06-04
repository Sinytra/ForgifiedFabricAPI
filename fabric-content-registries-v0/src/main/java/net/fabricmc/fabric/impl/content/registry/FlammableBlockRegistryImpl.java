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

package net.fabricmc.fabric.impl.content.registry;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class FlammableBlockRegistryImpl implements FlammableBlockRegistry {
	private static final FlammableBlockRegistry.Entry REMOVED = new FlammableBlockRegistry.Entry(0, 0);
	private static final Map<Block, FlammableBlockRegistryImpl> REGISTRIES = new HashMap<>();

	private final Map<Block, FlammableBlockRegistry.Entry> registeredEntriesBlock = new HashMap<>();
	private final Map<TagKey<Block>, FlammableBlockRegistry.Entry> registeredEntriesTag = new HashMap<>();
	private volatile Map<Block, FlammableBlockRegistry.Entry> computedEntries = null;
	private final Block key;

	private FlammableBlockRegistryImpl(Block key) {
		this.key = key;

		MinecraftForge.EVENT_BUS.addListener(this::onTagsLoaded);
	}
	
	private void onTagsLoaded(TagsUpdatedEvent event) {
		// Reset computed values after tags change since they depends on tags.
		computedEntries = null;
	}

	private Map<Block, FlammableBlockRegistry.Entry> getEntryMap() {
		Map<Block, FlammableBlockRegistry.Entry> ret = computedEntries;

		if (ret == null) {
			ret = new IdentityHashMap<>();

			// tags take precedence over blocks
			for (TagKey<Block> tag : registeredEntriesTag.keySet()) {
				FlammableBlockRegistry.Entry entry = registeredEntriesTag.get(tag);

				for (Block block : ForgeRegistries.BLOCKS.tags().getTag(tag)) {
					ret.put(block, entry);
				}
			}

			ret.putAll(registeredEntriesBlock);

			computedEntries = ret;
		}

		return ret;
	}

	// User-facing fire registry interface - queries vanilla fire block
	@Override
	public Entry get(Block block) {
		Entry entry = getEntryMap().get(block);

		if (entry != null) {
			return entry;
		} else {
			return ((FireBlockHooks) key).fabric_getVanillaEntry(block.defaultBlockState());
		}
	}

	public Entry getFabric(Block block) {
		return getEntryMap().get(block);
	}

	@Override
	public void add(Block block, Entry value) {
		registeredEntriesBlock.put(block, value);

		computedEntries = null;
	}

	@Override
	public void add(TagKey<Block> tag, Entry value) {
		registeredEntriesTag.put(tag, value);

		computedEntries = null;
	}

	@Override
	public void remove(Block block) {
		add(block, REMOVED);
	}

	@Override
	public void remove(TagKey<Block> tag) {
		add(tag, REMOVED);
	}

	@Override
	public void clear(Block block) {
		registeredEntriesBlock.remove(block);

		computedEntries = null;
	}

	@Override
	public void clear(TagKey<Block> tag) {
		registeredEntriesTag.remove(tag);

		computedEntries = null;
	}

	public static FlammableBlockRegistryImpl getInstance(Block block) {
		if (!(block instanceof FireBlockHooks)) {
			throw new RuntimeException("Not a hookable fire block: " + block);
		}

		return REGISTRIES.computeIfAbsent(block, FlammableBlockRegistryImpl::new);
	}
}
