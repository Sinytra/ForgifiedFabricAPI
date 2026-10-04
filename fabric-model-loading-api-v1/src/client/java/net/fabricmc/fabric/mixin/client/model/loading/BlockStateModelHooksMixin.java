package net.fabricmc.fabric.mixin.client.model.loading;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.client.model.block.BlockStateModelHooks;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.block.dispatch.SingleVariant.Unbaked;

import net.fabricmc.fabric.impl.client.model.loading.CustomUnbakedBlockStateModelRegistry;

@Mixin(BlockStateModelHooks.class)
public class BlockStateModelHooksMixin {
	@ModifyReturnValue(method = "makeSingleModelCodec", at = @At("RETURN"))
	private static MapCodec<Either<CustomUnbakedBlockStateModel, Unbaked>> wrapMakeSingleModelCodec(MapCodec<Either<CustomUnbakedBlockStateModel, Unbaked>> original) {
		return CustomUnbakedBlockStateModelRegistry.wrapMakeSingleModelCodec(original);
	}
}
