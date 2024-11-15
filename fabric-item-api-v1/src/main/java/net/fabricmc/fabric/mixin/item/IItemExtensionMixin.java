package net.fabricmc.fabric.mixin.item;

import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.impl.item.ItemExtensions;
import net.fabricmc.fabric.impl.item.ItemStackExtensions;
import net.fabricmc.fabric.impl.item.RecursivityHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IItemExtension.class)
public interface IItemExtensionMixin extends FabricItem {

    @Inject(method = "getCraftingRemainingItem", at = @At("HEAD"), cancellable = true)
    default void getCraftingRemainingItem(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack fabricRemainder = RecursivityHelper.nonRecursiveApiCall(() -> this.getRecipeRemainder(stack));
        if (!fabricRemainder.isEmpty()) {
            cir.setReturnValue(fabricRemainder);
        }
    }

    @Inject(method = "hasCraftingRemainingItem", at = @At("HEAD"), cancellable = true)
    default void hasCraftingRemainingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!RecursivityHelper.nonRecursiveApiCall(() -> this.getRecipeRemainder(stack)).isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEquipmentSlot", at = @At("HEAD"), cancellable = true)
    default void getEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        EquipmentSlotProvider equipmentSlotProvider = ((ItemExtensions) this).fabric_getEquipmentSlotProvider();

        if (equipmentSlotProvider != null) {
            cir.setReturnValue(equipmentSlotProvider.getPreferredEquipmentSlot(((ItemStackExtensions) (Object) stack).fabric_getLivingEntity(), stack));
        }
    }
}
