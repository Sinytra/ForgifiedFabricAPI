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

package net.fabricmc.fabric.impl.client.itemgroup;

import java.util.Set;
import java.util.function.Consumer;

import net.minecraftforge.common.CreativeModeTabRegistry;

import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;

public class FabricCreativeGuiComponents {
	public static final Set<ItemGroup> COMMON_GROUPS = Set.copyOf(CreativeModeTabRegistry.getDefaultTabs());

	public enum Type {
		NEXT(Text.literal(">"), CreativeGuiExtensions::fabric_nextPage),
		PREVIOUS(Text.literal("<"), CreativeGuiExtensions::fabric_previousPage);

		final Text text;
		final Consumer<CreativeGuiExtensions> clickConsumer;

		Type(Text text, Consumer<CreativeGuiExtensions> clickConsumer) {
			this.text = text;
			this.clickConsumer = clickConsumer;
		}
	}
}
