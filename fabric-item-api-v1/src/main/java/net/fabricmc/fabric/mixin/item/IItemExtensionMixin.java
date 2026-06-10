package net.fabricmc.fabric.mixin.item;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.impl.item.RecursivityHelper;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;

import net.minecraft.world.item.enchantment.Enchantment;

import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.impl.item.ItemExtensions;

@Mixin(IItemExtension.class)
public interface IItemExtensionMixin extends FabricItem {

    @Inject(method = "getCraftingRemainder", at = @At("HEAD"), cancellable = true)
    default void getCraftingRemainder(ItemInstance item, CallbackInfoReturnable<ItemStackTemplate> cir) {
		ItemStack stack = item instanceof ItemStack s ? s : new ItemStack(item.typeHolder(), item.count());
        ItemStackTemplate fabricRemainder = RecursivityHelper.nonRecursiveApiCall(() -> this.getCraftingRemainder(stack));
        if (fabricRemainder != null) {
            cir.setReturnValue(fabricRemainder);
        }
    }

    @Inject(method = "getEquipmentSlot", at = @At("HEAD"), cancellable = true)
    default void getEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        EquipmentSlotProvider equipmentSlotProvider = ((ItemExtensions) this).fabric_getEquipmentSlotProvider();

        if (equipmentSlotProvider != null) {
            cir.setReturnValue(equipmentSlotProvider.getEquipmentSlotForItem((LivingEntity) this, stack));
        }
    }
	
	@Inject(method = "isPrimaryItemFor", at = @At("HEAD"), cancellable = true)
	default void isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment, CallbackInfoReturnable<Boolean> cir) {
		if (RecursivityHelper.nonRecursiveApiCall(() -> stack.canBeEnchantedWith(enchantment, EnchantingContext.PRIMARY))) {
			cir.setReturnValue(true);
		}
	}
}
