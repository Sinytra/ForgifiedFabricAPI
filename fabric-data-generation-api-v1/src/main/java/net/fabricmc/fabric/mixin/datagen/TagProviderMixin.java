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

package net.fabricmc.fabric.mixin.datagen;

import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.util.Identifier;

@Mixin(TagProvider.class)
public class TagProviderMixin {
	@Redirect(method = "lambda$getOrCreateRawBuilder$9", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/data/ExistingFileHelper;trackGenerated(Lnet/minecraft/util/Identifier;Lnet/minecraftforge/common/data/ExistingFileHelper$IResourceType;)V"))
	public void fixExistingFileHelperNPE(@Nullable ExistingFileHelper existingFileHelper, Identifier identifier, ExistingFileHelper.IResourceType resourceType) {
		if (existingFileHelper != null) {
			existingFileHelper.trackGenerated(identifier, resourceType);
		}
	}
}
