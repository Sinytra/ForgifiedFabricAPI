package net.fabricmc.fabric.mixin.attachment;

import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AttachmentType.class)
public interface AttachmentTypeAccessor {
	@Accessor
	AttachmentSyncHandler<?> getSyncHandler();
}
