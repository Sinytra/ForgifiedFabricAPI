package net.fabricmc.fabric.mixin.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.RegistrationHelperImpl;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "onResourceManagerReload", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void createEntityRenderers(ResourceManager resourceManager, CallbackInfo ci, EntityRendererProvider.Context context) {
        ((EntityRenderDispatcher) (Object) this).renderers.forEach((entityType, renderer) -> {
            if (renderer instanceof LivingEntityRenderer livingEntityRenderer) { // Must be living for features
                LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker().registerRenderers((EntityType<? extends LivingEntity>) entityType, livingEntityRenderer, new RegistrationHelperImpl(livingEntityRenderer::addLayer), context);
            }
        });

        ((EntityRenderDispatcher) (Object) this).getSkinMap().forEach((name, renderer) ->
            LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker().registerRenderers(EntityType.PLAYER, (LivingEntityRenderer) renderer, new RegistrationHelperImpl(((LivingEntityRenderer) renderer)::addLayer), context));
    }
}
