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

package net.fabricmc.fabric.mixin.client.gametest.registry;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

// Restores the rearly return if "this.allTags.isBound()" is true from NeoForge <26.2
@Mixin(MappedRegistry.class)
public class MappedRegistryMixin<T> {
	@WrapOperation(method = "freeze", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/MappedRegistry$TagSet;isBound()Z"))
	private boolean captureTagsBound(@Coerce Object tagSet, Operation<Boolean> original, @Share("tagsBound") LocalBooleanRef tagsBound) {
		boolean bound = original.call(tagSet);
		tagsBound.set(bound);
		return bound;
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "freeze", at = @At(value = "FIELD", target = "Lnet/minecraft/core/MappedRegistry;frozenTags:Ljava/util/Map;", opcode = Opcodes.GETFIELD, ordinal = 0), cancellable = true)
	private void preserveBoundTags(CallbackInfoReturnable<Registry<T>> cir, @Share("tagsBound") LocalBooleanRef tagsBound) {
		if (tagsBound.get()) {
			cir.setReturnValue((Registry<T>) this);
		}
	}
}
