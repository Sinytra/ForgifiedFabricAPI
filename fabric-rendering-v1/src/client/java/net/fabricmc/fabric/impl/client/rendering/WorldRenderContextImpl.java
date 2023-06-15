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

package net.fabricmc.fabric.impl.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class WorldRenderContextImpl implements WorldRenderContext.BlockOutlineContext, WorldRenderContext {
    private LevelRenderer worldRenderer;
    private PoseStack matrixStack;
    private float tickDelta;
    private long limitTime;
    private boolean blockOutlines;
    private Camera camera;
    private Frustum frustum;
    private GameRenderer gameRenderer;
    private LightTexture lightmapTextureManager;
    private Matrix4f projectionMatrix;
    private MultiBufferSource consumers;
    private ProfilerFiller profiler;
    private boolean advancedTranslucency;
    private ClientLevel world;

    private Entity entity;
    private double cameraX;
    private double cameraY;
    private double cameraZ;
    private BlockPos blockPos;
    private BlockState blockState;

    public boolean renderBlockOutline = true;

    public void prepare(
        LevelRenderer worldRenderer,
        PoseStack matrixStack,
        float tickDelta,
        long limitTime,
        boolean blockOutlines,
        Camera camera,
        GameRenderer gameRenderer,
        LightTexture lightmapTextureManager,
        Matrix4f projectionMatrix,
        MultiBufferSource consumers,
        ProfilerFiller profiler,
        boolean advancedTranslucency,
        ClientLevel world
    ) {
        this.worldRenderer = worldRenderer;
        this.matrixStack = matrixStack;
        this.tickDelta = tickDelta;
        this.limitTime = limitTime;
        this.blockOutlines = blockOutlines;
        this.camera = camera;
        this.gameRenderer = gameRenderer;
        this.lightmapTextureManager = lightmapTextureManager;
        this.projectionMatrix = projectionMatrix;
        this.consumers = consumers;
        this.profiler = profiler;
        this.advancedTranslucency = advancedTranslucency;
        this.world = world;
    }

    public void setFrustum(Frustum frustum) {
        this.frustum = frustum;
    }

    public void prepareBlockOutline(
        Entity entity,
        double cameraX,
        double cameraY,
        double cameraZ,
        BlockPos blockPos,
        BlockState blockState
    ) {
        this.entity = entity;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
        this.blockPos = blockPos;
        this.blockState = blockState;
    }

    @Override
    public LevelRenderer worldRenderer() {
        return worldRenderer;
    }

    @Override
    public PoseStack matrixStack() {
        return matrixStack;
    }

    @Override
    public float tickDelta() {
        return tickDelta;
    }

    @Override
    public long limitTime() {
        return limitTime;
    }

    @Override
    public boolean blockOutlines() {
        return blockOutlines;
    }

    @Override
    public Camera camera() {
        return camera;
    }

    @Override
    public Matrix4f projectionMatrix() {
        return projectionMatrix;
    }

    @Override
    public ClientLevel world() {
        return world;
    }

    @Override
    public Frustum frustum() {
        return frustum;
    }

    @Override
    public @Nullable MultiBufferSource consumers() {
        return consumers;
    }

    @Override
    public GameRenderer gameRenderer() {
        return gameRenderer;
    }

    @Override
    public LightTexture lightmapTextureManager() {
        return lightmapTextureManager;
    }

    @Override
    public ProfilerFiller profiler() {
        return profiler;
    }

    @Override
    public boolean advancedTranslucency() {
        return advancedTranslucency;
    }

    @Override
    public VertexConsumer vertexConsumer() {
        return consumers.getBuffer(RenderType.lines());
    }

    @Override
    public Entity entity() {
        return entity;
    }

    @Override
    public double cameraX() {
        return cameraX;
    }

    @Override
    public double cameraY() {
        return cameraY;
    }

    @Override
    public double cameraZ() {
        return cameraZ;
    }

    @Override
    public BlockPos blockPos() {
        return blockPos;
    }

    @Override
    public BlockState blockState() {
        return blockState;
    }
}
