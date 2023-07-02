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

package net.fabricmc.fabric.test.access.client;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;

import net.fabricmc.fabric.test.access.SignBlockEntityTest;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class BlockEntityRendererTest {

	public static void onInitializeClient(FMLClientSetupEvent event) {
		BlockEntityRendererFactories.register(SignBlockEntityTest.TEST_SIGN_BLOCK_ENTITY.get(), SignBlockEntityRenderer::new);
	}
}
