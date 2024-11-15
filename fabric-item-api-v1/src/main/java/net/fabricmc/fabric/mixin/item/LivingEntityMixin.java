package net.fabricmc.fabric.mixin.item;

import net.fabricmc.fabric.impl.item.ItemStackExtensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Inject(method = "getEquipmentSlotForItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEquipmentSlot()Lnet/minecraft/world/entity/EquipmentSlot;"))
	private void storeLivingEntity(ItemStack arg, CallbackInfoReturnable<EquipmentSlot> cir) {
		((ItemStackExtensions) (Object) arg).fabric_setLivingEntity((LivingEntity) (Object) this);
	}

	@Inject(method = "getEquipmentSlotForItem", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;getEquipmentSlot()Lnet/minecraft/world/entity/EquipmentSlot;"))
	private void resetLivingEntity(ItemStack arg, CallbackInfoReturnable<EquipmentSlot> cir) {
		((ItemStackExtensions) (Object) arg).fabric_setLivingEntity(null);
	}
}
