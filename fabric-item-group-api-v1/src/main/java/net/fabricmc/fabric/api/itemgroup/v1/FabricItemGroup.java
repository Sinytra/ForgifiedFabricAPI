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

package net.fabricmc.fabric.api.itemgroup.v1;

import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupBuilderImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Contains a method to create an item group builder.
 */
public final class FabricItemGroup {
	private FabricItemGroup() {
	}

	/**
	 * Creates a new builder for {@link CreativeModeTab}. Item groups are used to group items in the creative
	 * inventory.
	 *
	 * <p>Each new {@link CreativeModeTab} instance of this class is automatically appended to {@link net.minecraft.world.item.CreativeModeTabs#allTabs()} when
	 * {@link CreativeModeTab.Builder#build()} is invoked.
	 *
	 * <p>Example:
	 *
	 * <pre>{@code
	 * private static final ItemGroup ITEM_GROUP = FabricItemGroup.builder(new Identifier(MOD_ID, "test_group"))
	 *    .icon(() -> new ItemStack(Items.DIAMOND))
	 *    .entries((enabledFeatures, entries, operatorEnabled) -> {
	 *       entries.add(TEST_ITEM);
	 *    })
	 *    .build();
	 * }</pre>
	 *
	 * @param identifier the id of the ItemGroup, to be used as the default translation key
	 * @return a new {@link CreativeModeTab} instance
	 */
	public static CreativeModeTab.Builder builder(ResourceLocation identifier) {
		return new FabricItemGroupBuilderImpl(identifier)
			.title(Component.translatable("itemGroup.%s.%s".formatted(identifier.getNamespace(), identifier.getPath())));
	}
}
