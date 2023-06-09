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

package net.fabricmc.fabric.test.model;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_models_v0_testmod")
public class ModelTestModClient {
	public static final String MODID = "fabric_models_v0_testmod";

	public static final ResourceLocation MODEL_ID = new ResourceLocation(MODID, "half_red_sand");

	public ModelTestModClient() {
		ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
			out.accept(MODEL_ID);
		});

		// TODO
//		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(SpecificModelReloadListener.INSTANCE);
//
//		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
//			if (entityRenderer instanceof PlayerRenderer playerRenderer) {
//				registrationHelper.register(new BakedModelFeatureRenderer<>(playerRenderer, SpecificModelReloadListener.INSTANCE::getSpecificModel));
//			}
//		});
	}
}
