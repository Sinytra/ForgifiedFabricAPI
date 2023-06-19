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

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.impl.client.indigo.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessChunkRendererRegion;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

/**
 * Implements the main hooks for terrain rendering. Attempts to tread
 * lightly. This means we are deliberately stepping over some minor
 * optimization opportunities.
 *
 * <p>Non-Fabric renderer implementations that are looking to maximize
 * performance will likely take a much more aggressive approach.
 * For that reason, mod authors who want compatibility with advanced
 * renderers will do well to steer clear of chunk rebuild hooks unless
 * they are creating a renderer.
 *
 * <p>These hooks are intended only for the Fabric default renderer and
 * aren't expected to be present when a different renderer is being used.
 * Renderer authors are responsible for creating the hooks they need.
 * (Though they can use these as an example if they wish.)
 */
@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public abstract class ChunkBuilderBuiltChunkRebuildTaskMixin {
    @Final
    @Shadow(aliases = "this$1")
    ChunkRenderDispatcher.RenderChunk f_112859_;

    @Inject(method = "compile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;betweenClosed(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Ljava/lang/Iterable;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void hookChunkBuild(float cameraX, float cameraY, float cameraZ,
								ChunkBufferBuilderPack builder,
								CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults> ci,
								ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults renderData, int i, BlockPos blockPos, BlockPos blockPos2, VisGraph chunkOcclusionDataBuilder, RenderChunkRegion region, PoseStack matrixStack, Set<RenderType> initializedLayers, RandomSource abstractRandom, BlockRenderDispatcher blockRenderManager) {
        // hook just before iterating over the render chunk's chunks blocks, captures the used renderlayer set
        // accessing this.region is unsafe due to potential async cancellation, the LV has to be used!

        TerrainRenderContext renderer = TerrainRenderContext.POOL.get();
        renderer.prepare(region, f_112859_, renderData, builder, initializedLayers);
        ((AccessChunkRendererRegion) region).fabric_setRenderer(renderer);
    }

    /**
     * This is the hook that actually implements the rendering API for terrain rendering.
     *
     * <p>It's unusual to have a @Redirect in a Fabric library, but in this case
     * it is our explicit intention that {@link BlockRenderDispatcher#renderBatched(BlockState, BlockPos, BlockAndTintGetter, PoseStack, VertexConsumer, boolean, RandomSource)}
     * does not execute for models that will be rendered by our renderer.
     *
     * <p>Any mod that wants to redirect this specific call is likely also a renderer, in which case this
     * renderer should not be present, or the mod should probably instead be relying on the renderer API
     * which was specifically created to provide for enhanced terrain rendering.
     *
     * <p>Note also that {@link BlockRenderDispatcher#renderBatched(BlockState, BlockPos, BlockAndTintGetter, PoseStack, VertexConsumer, boolean, RandomSource)}
     * IS called if the block render type is something other than {@link net.minecraft.world.level.block.RenderShape#MODEL}.
     * Normally this does nothing but will allow mods to create rendering hooks that are
     * driven off of render type. (Not recommended or encouraged, but also not prevented.)
     */
    @Redirect(method = "compile", require = 1, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;Z)V"))
    private void hookChunkBuildTesselate(BlockRenderDispatcher renderManager, BlockState blockState, BlockPos blockPos, BlockAndTintGetter blockView, PoseStack matrix, VertexConsumer bufferBuilder, boolean checkSides, RandomSource random, ModelData modelData, RenderType renderType, boolean queryModelSpecificData) {
        if (blockState.getRenderShape() == RenderShape.MODEL) {
            final BakedModel model = renderManager.getBlockModel(blockState);

            if (Indigo.ALWAYS_TESSELATE_INDIGO || !((FabricBakedModel) model).isVanillaAdapter()) {
                Vec3 vec3d = blockState.getOffset(blockView, blockPos);
                matrix.translate(vec3d.x, vec3d.y, vec3d.z);
                ((AccessChunkRendererRegion) blockView).fabric_getRenderer().tessellateBlock(blockState, blockPos, model, matrix);
                return;
            }
        }

        renderManager.renderBatched(blockState, blockPos, blockView, matrix, bufferBuilder, checkSides, random);
    }

    /**
     * Release all references. Probably not necessary but would be $#%! to debug if it is.
     */
    @Inject(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;clearCache()V"))
    private void hookRebuildChunkReturn(CallbackInfoReturnable<Set<BlockEntity>> ci) {
        // hook after iterating over the render chunk's chunks blocks, must be called if and only if hookChunkBuild happened

        TerrainRenderContext.POOL.get().release();
    }
}
