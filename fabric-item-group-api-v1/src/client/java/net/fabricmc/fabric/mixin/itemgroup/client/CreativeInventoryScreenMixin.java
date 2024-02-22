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

package net.fabricmc.fabric.mixin.itemgroup.client;

import net.fabricmc.fabric.impl.client.itemgroup.CreativeGuiExtensions;

import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;

import net.fabricmc.fabric.impl.itemgroup.FabricItemGroup;

import net.minecraft.item.ItemGroups;

import net.minecraftforge.client.gui.CreativeTabsScreenPage;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Mixin(value = CreativeInventoryScreen.class, priority = 100)
public abstract class CreativeInventoryScreenMixin implements CreativeGuiExtensions {
	@Shadow(remap = false)
	private CreativeTabsScreenPage currentPage;
	@Shadow(remap = false)
	@Final
	private List<CreativeTabsScreenPage> pages;

	@Shadow(remap = false)
	public abstract void setCurrentPage(CreativeTabsScreenPage currentPage);

	private static int fabric_currentPage = -1;

	private void fabric_updateSelection() {
	}

	private boolean fabric_isGroupVisible(ItemGroup tab) {
		return this.currentPage.getVisibleTabs().contains(tab);
	}

	private static int fabric_getPage() {
		return fabric_currentPage;
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void initPages(CallbackInfo ci, int tabIndex, List<ItemGroup> currentPage, Iterator<?> it, ItemGroup sortedCreativeModeTab) {
		((FabricItemGroup) sortedCreativeModeTab).setPage(this.pages.size());
	}

	@Override
	public void fabric_previousPage() {
		this.setCurrentPage(this.pages.get(Math.max(this.pages.indexOf(this.currentPage) - 1, 0)));
	}

	@Override
	public void fabric_nextPage() {
		this.setCurrentPage(this.pages.get(Math.min(this.pages.indexOf(this.currentPage) + 1, this.pages.size() - 1)));
	}

	@Override
	public boolean fabric_isButtonVisible(FabricCreativeGuiComponents.Type type) {
		return ItemGroups.getGroupsToDisplay().size() > (Objects.requireNonNull(ItemGroups.displayContext).hasPermissions() ? 14 : 13);
	}

	@Override
	public boolean fabric_isButtonEnabled(FabricCreativeGuiComponents.Type type) {
		if (type == FabricCreativeGuiComponents.Type.NEXT) {
			return this.pages.indexOf(this.currentPage) < this.pages.size() - 1;
		}

		if (type == FabricCreativeGuiComponents.Type.PREVIOUS) {
			return this.pages.indexOf(currentPage) != 0;
		}

		return false;
	}

	@Override
	public int fabric_currentPage() {
		return this.pages.indexOf(currentPage);
	}
}
