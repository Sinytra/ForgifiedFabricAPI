package net.fabricmc.fabric.mixin.attachment;

import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.fabric.impl.attachment.AttachmentChangeEvents;

@Mixin(AttachmentHolder.class)
public class AttachmentHolderMixin {

	@Inject(method = "setData", at = @At("RETURN"))
	private <T> void onSetData(AttachmentType<T> type, T data, CallbackInfoReturnable<T> cir) {
		AttachmentChangeEvents.invoke(type, cir.getReturnValue(), data);
	}

	@Inject(method = "removeData", at = @At("RETURN"))
	private <T> void onRemoveData(AttachmentType<T> type, CallbackInfoReturnable<T> cir) {
		AttachmentChangeEvents.invoke(type, cir.getReturnValue(), null);
	}
}
