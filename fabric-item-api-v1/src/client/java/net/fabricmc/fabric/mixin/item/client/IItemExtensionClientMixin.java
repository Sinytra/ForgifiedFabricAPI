package net.fabricmc.fabric.mixin.item.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.impl.item.RecursivityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IItemExtension.class)
public interface IItemExtensionClientMixin {

    @ModifyReturnValue(method = "shouldCauseReequipAnimation", at = @At("RETURN"))
    default boolean shouldCauseReequipAnimation(boolean result, ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (result) {
            Player player = Minecraft.getInstance().player;
            InteractionHand hand = oldStack == player.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            return RecursivityHelper.nonRecursiveApiCall(() -> ((FabricItem) this).allowComponentsUpdateAnimation(player, hand, oldStack, newStack));
        }
        return false;
    }

    @Inject(method = "shouldCauseBlockBreakReset", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamageableItem()Z"), cancellable = true)
    default void shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack, CallbackInfoReturnable<Boolean> cir) {
        if (!ItemStack.isSameItemSameComponents(newStack, oldStack) && oldStack.getItem().allowContinuingBlockBreaking(Minecraft.getInstance().player, oldStack, newStack)) {
            cir.setReturnValue(false);
        }
    }
}
