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

package net.fabricmc.fabric.api.client.model;

import net.fabricmc.fabric.impl.client.model.BakedModelManagerHooks;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class BakedModelManagerHelper {
	/**
	 * An alternative to {@link ModelManager#getModel(net.minecraft.client.resources.model.ModelResourceLocation)} that accepts an
	 * {@link ResourceLocation} instead. Models loaded using {@link ExtraModelProvider} do not have a
	 * corresponding {@link net.minecraft.client.resources.model.ModelResourceLocation}, so the vanilla method cannot be used to retrieve them.
	 * The Identifier that was used to load them can be used in this method to retrieve them.
	 *
	 * <p><b>This method, as well as its vanilla counterpart, should only be used after the
	 * {@link ModelManager} has completed reloading.</b> Otherwise, the result will be
	 * null or an old model.
	 *
	 * @param manager the manager that holds models
	 * @param id the id of the model
	 * @return the model
	 */
	@Nullable
	public static BakedModel getModel(ModelManager manager, ResourceLocation id) {
		return ((BakedModelManagerHooks) manager).fabric_getModel(id);
	}

	private BakedModelManagerHelper() { }
}
