package net.fabricmc.fabric.mixin.attachment;

import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;

@Mixin(IAttachmentHolder.class)
public interface IAttachmentHolderMixin extends AttachmentTarget {

}
