package net.fabricmc.fabric.mixin.recipe.sync;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.network.payload.RecipeContentPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {
	@ModifyExpressionValue(method = "sendRecipes", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/payload/RecipeContentPayload;create(Ljava/util/Collection;Lnet/minecraft/world/item/crafting/RecipeMap;)Lnet/neoforged/neoforge/network/payload/RecipeContentPayload;"))
	private static RecipeContentPayload sendRecipes(RecipeContentPayload payload, ServerPlayer player) {
		return RecipeSyncImpl.appendSyncedRecipes(payload, player);
	}
}
