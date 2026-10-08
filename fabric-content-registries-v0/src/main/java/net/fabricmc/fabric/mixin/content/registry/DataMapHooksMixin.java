package net.fabricmc.fabric.mixin.content.registry;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.neoforged.neoforge.common.DataMapHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;

@Mixin(DataMapHooks.class)
public abstract class DataMapHooksMixin {
	@ModifyReturnValue(method = "getNextOxidizedStage", at = @At("RETURN"))
	private static Block fallbackToVanillaNextOxidation(Block original, Block previousStage) {
		if (original == null) {
			return WeatheringCopper.NEXT_BY_BLOCK.get().get(previousStage);
		}

		return original;
	}

	@ModifyReturnValue(method = "getPreviousOxidizedStage", at = @At("RETURN"))
	private static Block fallbackToVanillaPreviousOxidation(Block original, Block nextStage) {
		if (original == null) {
			return WeatheringCopper.PREVIOUS_BY_BLOCK.get().get(nextStage);
		}

		return original;
	}

	@ModifyReturnValue(method = "getBlockWaxed", at = @At("RETURN"))
	private static Block fallbackToVanillaWaxed(Block original, Block unwaxed) {
		if (original == null) {
			return HoneycombItem.WAXABLES.get().get(unwaxed);
		}

		return original;
	}

	@ModifyReturnValue(method = "getBlockUnwaxed", at = @At("RETURN"))
	private static Block fallbackToVanillaUnwaxed(Block original, Block waxed) {
		if (original == null) {
			return HoneycombItem.WAX_OFF_BY_BLOCK.get().get(waxed);
		}

		return original;
	}
}
