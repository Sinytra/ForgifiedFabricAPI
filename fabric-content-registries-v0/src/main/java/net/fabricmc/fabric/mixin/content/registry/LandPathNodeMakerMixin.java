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

package net.fabricmc.fabric.mixin.content.registry;

import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// Applied a bit earlier than other mods to ensure changes and optimizations to default vanilla behavior
@Mixin(value = WalkNodeEvaluator.class, priority = 999)
public class LandPathNodeMakerMixin {
	/**
	 * Overrides the node type for the specified position, if the position is a direct target in a path.
	 */
	@Inject(method = "getBlockPathTypeRaw", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private static void getCommonNodeType(BlockGetter world, BlockPos pos, CallbackInfoReturnable<BlockPathTypes> cir, BlockState state) {
		BlockPathTypes nodeType = LandPathNodeTypesRegistry.getPathNodeType(state, world, pos, false);

		if (nodeType != null) {
			cir.setReturnValue(nodeType);
		}
	}

	/**
	 * Overrides the node type for the specified position, if the position is found as neighbor block in a path.
	 */
	@Inject(method = "checkNeighbourBlocks", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private static void getNodeTypeFromNeighbors(BlockGetter world, BlockPos.MutableBlockPos pos, BlockPathTypes nodeType, CallbackInfoReturnable<BlockPathTypes> cir, int i, int j, int k, int l, int m, int n, BlockState state) {
		BlockPathTypes neighborNodeType = LandPathNodeTypesRegistry.getPathNodeType(state, world, pos, true);

		if (neighborNodeType != null) {
			cir.setReturnValue(neighborNodeType);
		}
	}
}
