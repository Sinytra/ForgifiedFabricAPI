package net.fabricmc.fabric.mixin.client.rendering.fluid;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends AbstractBlock.AbstractBlockState implements IForgeBlockState {

	protected BlockStateMixin(Block block, ImmutableMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> codec) {
		super(block, propertyMap, codec);
	}

	@Override
	public boolean shouldDisplayFluidOverlay(BlockRenderView level, BlockPos pos, FluidState fluidState) {
		return FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent((BlockState) (Object) this, level, pos, fluidState);
	}
}
