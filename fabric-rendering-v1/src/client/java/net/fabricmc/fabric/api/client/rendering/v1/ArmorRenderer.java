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

package net.fabricmc.fabric.api.client.rendering.v1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Armor renderers render worn armor items with custom code.
 * They may be used to render armor with special models or effects.
 *
 * <p>The renderers are registered with {@link net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer#register(ArmorRenderer, ItemLike...)}.
 */
@FunctionalInterface
public interface ArmorRenderer {
	/**
	 * Registers the armor renderer for the specified items.
	 * @param renderer	the renderer
	 * @param items		the items
	 * @throws IllegalArgumentException if an item already has a registered armor renderer
	 * @throws NullPointerException if either an item or the renderer is null
	 */
	static void register(ArmorRenderer renderer, ItemLike... items) {
		ArmorRendererRegistryImpl.register(renderer, items);
	}

	/**
	 * Helper method for rendering a specific armor model, comes after setting visibility.
	 *
	 * <p>This primarily handles applying glint and the correct {@link RenderType}
	 * @param matrices			the matrix stack
	 * @param vertexConsumers	the vertex consumer provider
	 * @param light				packed lightmap coordinates
	 * @param stack				the item stack of the armor item
	 * @param model				the model to be rendered
	 * @param texture			the texture to be applied
	 */
	static void renderPart(PoseStack matrices, MultiBufferSource vertexConsumers, int light, ItemStack stack, Model model, ResourceLocation texture) {
		VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, RenderType.armorCutoutNoCull(texture), false, stack.hasFoil());
		model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
	}

	/**
	 * Renders an armor part.
	 *
	 * @param matrices			the matrix stack
	 * @param vertexConsumers	the vertex consumer provider
	 * @param stack				the item stack of the armor item
	 * @param entity			the entity wearing the armor item
	 * @param slot				the equipment slot in which the armor stack is worn
	 * @param light				packed lightmap coordinates
	 * @param contextModel		the model provided by {@link RenderLayer#getParentModel()}
	 */
	void render(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<LivingEntity> contextModel);
}
