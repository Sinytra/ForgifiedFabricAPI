package net.fabricmc.fabric.mixin.client.rendering.fluid;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements IForgeBlockState {
    
    protected BlockStateMixin(Block owner, ImmutableMap<Property<?>, Comparable<?>> values, MapCodec<BlockState> propertiesCodec) {
        super(owner, values, propertiesCodec);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent((BlockState) (Object) this, level, pos, fluidState);
    }
}
