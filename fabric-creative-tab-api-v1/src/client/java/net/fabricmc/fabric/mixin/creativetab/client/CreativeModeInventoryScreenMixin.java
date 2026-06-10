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

package net.fabricmc.fabric.mixin.creativetab.client;

import java.util.List;

import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;

import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<ItemPickerMenu> implements FabricCreativeModeInventoryScreen {
	@Shadow
	private static CreativeModeTab selectedTab;

	@Shadow
	@Final
	private List<CreativeTabsScreenPage> pages;
	@Shadow
	private CreativeTabsScreenPage currentPage;

	public CreativeModeInventoryScreenMixin(ItemPickerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public boolean switchToPage(int page) {
		CreativeTabsScreenPage oldPage = currentPage;
		((CreativeModeInventoryScreen) (Object) this).setCurrentPage(pages.get(page));
		return oldPage != currentPage;
	}

	@Override
	public boolean switchToNextPage() {
		CreativeTabsScreenPage oldPage = currentPage;
		((CreativeModeInventoryScreen) (Object) this).setCurrentPage(this.pages.get(Math.min(this.pages.indexOf(this.currentPage) + 1, this.pages.size() - 1)));
		return oldPage != currentPage;
	}

	@Override
	public boolean switchToPreviousPage() {
		CreativeTabsScreenPage oldPage = currentPage;
		((CreativeModeInventoryScreen) (Object) this).setCurrentPage(this.pages.get(Math.max(this.pages.indexOf(this.currentPage) - 1, 0)));
		return oldPage != currentPage;
	}

	@Override
	public int getPageCount() {
		return pages.size();
	}

	@Override
	public List<CreativeModeTab> getTabsOnPage(int page) {
		return pages.get(page).getVisibleTabs();
	}

	@Override
	public int getPage(CreativeModeTab creativeModeTab) {
		for (int i = 0; i < pages.size(); i++) {
			CreativeTabsScreenPage page = pages.get(i);

			if (page.getVisibleTabs().contains(creativeModeTab)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean hasAdditionalPages() {
		return pages.size() > 1;
	}

	@Override
	public CreativeModeTab getSelectedTab() {
		return selectedTab;
	}

	@Override
	public boolean setSelectedTab(CreativeModeTab creativeModeTab) {
		if (selectedTab != creativeModeTab) {
			selectedTab = creativeModeTab;
			return true;
		}
		return false;
	}
}
