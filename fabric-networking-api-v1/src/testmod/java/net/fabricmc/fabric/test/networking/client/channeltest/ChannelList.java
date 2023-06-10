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

package net.fabricmc.fabric.test.networking.client.channeltest;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class ChannelList extends AbstractSelectionList<ChannelList.Entry> {
	ChannelList(Minecraft client, int width, int height, int top, int bottom, int itemHeight) {
		super(client, width, height, top, bottom, itemHeight);
	}

	@Override
	public int addEntry(Entry entry) {
		return super.addEntry(entry);
	}

	void clear() {
		this.clearEntries();
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		// TODO seems to be possibly accessibility related
	}

	class Entry extends AbstractSelectionList.Entry<Entry> {
		private final ResourceLocation channel;

		Entry(ResourceLocation channel) {
			this.channel = channel;
		}

		@Override
		public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			ChannelList.this.minecraft.font.draw(matrices, Component.literal(this.channel.toString()), x, y, ChatFormatting.WHITE.getColor());
		}
	}
}
