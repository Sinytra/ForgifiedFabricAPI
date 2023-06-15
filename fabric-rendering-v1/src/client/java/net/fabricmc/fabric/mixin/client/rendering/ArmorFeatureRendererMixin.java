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

package net.fabricmc.fabric.mixin.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>> {

	private ArmorFeatureRendererMixin(RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> parent) {
		super(parent);
	}

	@Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
	private void renderArmor(PoseStack matrices, MultiBufferSource vertexConsumers, LivingEntity entity, EquipmentSlot armorSlot, int light, HumanoidModel<LivingEntity> model, CallbackInfo ci) {
		ItemStack stack = entity.getItemBySlot(armorSlot);
		ArmorRenderer renderer = ArmorRendererRegistryImpl.get(stack.getItem());

		if (renderer != null) {
			renderer.render(matrices, vertexConsumers, stack, entity, armorSlot, light, getParentModel());
			ci.cancel();
		}
	}
}
