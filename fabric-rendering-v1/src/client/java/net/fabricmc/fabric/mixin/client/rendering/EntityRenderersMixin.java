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

import com.google.common.collect.ImmutableMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.RegistrationHelperImpl;

@Mixin(EntityRenderers.class)
public abstract class EntityRenderersMixin {

	// synthetic lambda in reloadEntityRenderers
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Redirect(method = { "method_32174(Lcom/google/common/collect/ImmutableMap$Builder;Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Lnet/minecraft/entity/EntityType;Lnet/minecraft/client/render/entity/EntityRendererFactory;)V", "lambda$createEntityRenderers$2" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRendererFactory;create(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;)Lnet/minecraft/client/render/entity/EntityRenderer;"), require = 1)
	private static EntityRenderer<?> createEntityRenderer(EntityRendererFactory<?> entityRendererFactory, EntityRendererFactory.Context context, ImmutableMap.Builder builder, EntityRendererFactory.Context context2, EntityType<?> entityType) {
		EntityRenderer<?> entityRenderer = entityRendererFactory.create(context);

		if (entityRenderer instanceof LivingEntityRenderer) { // Must be living for features
			LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker().registerRenderers((EntityType<? extends LivingEntity>) entityType, (LivingEntityRenderer) entityRenderer, new RegistrationHelperImpl(((LivingEntityRenderer) entityRenderer)::addFeature), context);
		}

		return entityRenderer;
	}

	// private static synthetic method_32175(Lcom/google/common/collect/ImmutableMap$Builder;Lnet/minecraft/class_5617$class_5618;Ljava/lang/String;Lnet/minecraft/class_5617;)V
	@SuppressWarnings("rawtypes")
	@Redirect(method = { "method_32175(Lcom/google/common/collect/ImmutableMap$Builder;Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Ljava/lang/String;Lnet/minecraft/client/render/entity/EntityRendererFactory;)V", "lambda$createPlayerRenderers$3" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRendererFactory;create(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;)Lnet/minecraft/client/render/entity/EntityRenderer;"), require = 1)
	private static EntityRenderer<? extends PlayerEntity> createPlayerEntityRenderer(EntityRendererFactory<AbstractClientPlayerEntity> playerEntityRendererFactory, EntityRendererFactory.Context context, ImmutableMap.Builder builder, EntityRendererFactory.Context context2, String str, EntityRendererFactory<AbstractClientPlayerEntity> playerEntityRendererFactory2) {
		EntityRenderer<? extends PlayerEntity> entityRenderer = playerEntityRendererFactory.create(context);

		LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker().registerRenderers(EntityType.PLAYER, (LivingEntityRenderer) entityRenderer, new RegistrationHelperImpl(((LivingEntityRenderer) entityRenderer)::addFeature), context);

		return entityRenderer;
	}
}
