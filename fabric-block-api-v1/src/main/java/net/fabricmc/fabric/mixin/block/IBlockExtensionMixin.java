package net.fabricmc.fabric.mixin.block;

import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.block.v1.FabricBlock;

@Mixin(IBlockExtension.class)
public interface IBlockExtensionMixin extends FabricBlock {
}
