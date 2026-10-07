package net.fabricmc.fabric.mixin.block;

import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.fabricmc.fabric.impl.block.EnchantmentPowerBridge;

@Mixin(IBlockExtension.class)
public interface IBlockExtensionMixin extends FabricBlock {
	@Inject(method = "getEnchantPowerBonus", at = @At("HEAD"), cancellable = true)
	default void getEnchantPowerBonus(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if (EnchantmentPowerBridge.isFabricCall()) {
			cir.setReturnValue(EnchantmentPowerBridge.FABRIC_DEFAULT_POWER);
		} else if (state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER)) {
			cir.setReturnValue(EnchantmentPowerBridge.callFabric(() -> this.getProvidedEnchantmentPower(state, level, pos)));
		}
	}
}
