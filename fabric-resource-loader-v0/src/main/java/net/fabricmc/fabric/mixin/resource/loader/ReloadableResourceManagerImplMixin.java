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

package net.fabricmc.fabric.mixin.resource.loader;

import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerImplMixin {
	// private static synthetic m_203825_(Ljava/util/List;)Ljava/lang/Object;
	// Supplier lambda in createReload method.
	@Inject(method = {"m_203825_", "lambda$createReload$0"}, at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private static void getResourcePackNames(List<PackResources> packs, CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(packs.stream().map(pack -> {
			if (pack instanceof GroupResourcePack groupResourcePack) {
				return groupResourcePack.getFullName();
			} else {
				return pack.packId();
			}
		}).collect(Collectors.joining(", ")));
	}
}
