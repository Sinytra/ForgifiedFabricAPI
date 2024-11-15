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

package net.fabricmc.fabric.mixin.item;

import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.item.ItemStackExtensions;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.fabricmc.fabric.impl.item.ItemExtensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtensions, FabricItemStack {
	@Unique
	@Nullable
	private LivingEntity livingEntity;

	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract void shrink(int amount);

	@WrapOperation(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	private void hookDamage(ItemStack instance, int amount, ServerLevel serverWorld, LivingEntity serverPlayerEntity, Consumer<Item> consumer, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) EquipmentSlot slot) {
		CustomDamageHandler handler = ((ItemExtensions) getItem()).fabric_getCustomDamageHandler();

		/*
			This is called by creative mode players, post-24w21a.
			The other damage method (which original.call discards) handles the creative mode check.
			Since it doesn't make sense to call an event to calculate a to-be-discarded value
			(and to prevent mods from breaking item stacks in Creative mode),
			we preserve the pre-24w21a behavior of not calling in creative mode.
		*/

		if (handler != null && !entity.hasInfiniteMaterials()) {
			// Track whether an item has been broken by custom handler
			MutableBoolean mut = new MutableBoolean(false);
			amount = handler.damage((ItemStack) (Object) this, amount, entity, slot, () -> {
				mut.setTrue();
				this.shrink(1);
				consumer.accept(this.getItem());
			});

			// If item is broken, there's no reason to call the original.
			if (mut.booleanValue()) return;
		}

		original.call(instance, amount, serverWorld, serverPlayerEntity, consumer);
	}

	@Override
	public @Nullable LivingEntity fabric_getLivingEntity() {
		return livingEntity;
	}

	@Override
	public void fabric_setLivingEntity(LivingEntity livingEntity) {
		this.livingEntity = livingEntity;
	}
}
