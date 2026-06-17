package net.fabricmc.fabric.mixin.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IAttachmentHolder.class)
public interface IAttachmentHolderMixin extends AttachmentTarget {

}
