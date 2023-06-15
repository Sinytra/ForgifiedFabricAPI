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

package net.fabricmc.fabric.test.rendering.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FeatureRendererTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureRendererTest.class);
	private static int playerRegistrations = 0;

	public static void onInitializeClient() {
		LOGGER.info("Registering feature renderer tests");
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			// minecraft:player SHOULD be printed twice
			LOGGER.info(String.format("Received registration for %s", ForgeRegistries.ENTITY_TYPES.getKey(entityType)));

			if (entityType == EntityType.PLAYER) {
				playerRegistrations++;
			}

			if (entityRenderer instanceof PlayerRenderer renderer) {
				registrationHelper.register(new TestPlayerFeatureRenderer(renderer));
			}
		});

		// FIXME: Add AfterResourceReload event to client so this can be tested.
		//  This is due to a change in 20w45a which now means this is called after the client is initialized.
		/*ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			LOGGER.info("Client is starting");

			if (this.playerRegistrations != 2) {
				throw new AssertionError(String.format("Expected 2 entity feature renderer registration events for \"minecraft:player\" but received %s registrations", this.playerRegistrations));
			}

			LOGGER.info("Successfully called feature renderer registration events");
		});*/
	}

	private static class TestPlayerFeatureRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
		TestPlayerFeatureRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> context) {
			super(context);
		}

		@Override
		public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
			matrices.pushPose();

			// Translate to center above the player's head
			matrices.translate(-0.5F, -entity.getBbHeight() + 0.25F, -0.5F);
			// Render a diamond block above the player's head
			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.DIAMOND_BLOCK.defaultBlockState(), matrices, vertexConsumers, light, OverlayTexture.NO_OVERLAY);

			matrices.popPose();
		}
	}
}
