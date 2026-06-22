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

package net.fabricmc.fabric.impl.client.model.loading;

import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.mixin.client.model.loading.BlockStateModelHooksAccessor;

public class CustomUnbakedBlockStateModelRegistry {
	public static final String TYPE_KEY = "fabric:type";

	public static void register(Identifier id, MapCodec<? extends net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel> codec) {
		BlockStateModelHooksAccessor.getBlockStateModelIDs().put(id, wrapCodec(codec));
	}

	@SuppressWarnings("unchecked")
	public static MapCodec<? extends CustomUnbakedBlockStateModel> wrapCodec(
			MapCodec<? extends net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel> fabricCodec
	) {
		return ((MapCodec<net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel>) fabricCodec)
				.xmap(FabricCustomUnbakedModel::new, FabricCustomUnbakedModel::getInner);
	}

	public static MapCodec<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>> wrapMakeSingleModelCodec(
			MapCodec<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>> original
	) {
		var nested = NeoForgeExtraCodecs.dispatchMapOrElse(
				TYPE_KEY,
				BlockStateModelHooksAccessor.getBlockStateModelIDs().codec(Identifier.CODEC),
				CustomUnbakedBlockStateModel::codec,
				Function.identity(),
				original);
		return nested.xmap(
				either -> either.map(Either::left, Function.identity()),
				u -> Either.right(u)
		);
	}
}
