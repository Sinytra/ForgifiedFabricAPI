package net.fabricmc.fabric.mixin.client.model.loading;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModelBakery.class, priority = 1)
public class ModelLoaderLateMixin {
    // This is the call that needs to be redirected to support ModelResolvers, but it returns a JsonUnbakedModel.
    // Redirect it to always return null and handle the logic in a ModifyVariable right after the call.
    @Redirect(method = "getModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;loadBlockModel(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/block/model/BlockModel;"), require = 0)
    private BlockModel cancelLoadModelFromJson(ModelBakery self, ResourceLocation id) {
        return null;
    }
}
