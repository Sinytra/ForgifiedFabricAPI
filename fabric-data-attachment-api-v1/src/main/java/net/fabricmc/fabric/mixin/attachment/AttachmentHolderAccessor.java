package net.fabricmc.fabric.mixin.attachment;

import java.util.Map;

import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AttachmentHolder.class)
public interface AttachmentHolderAccessor {
	@Accessor
	Map<AttachmentType<?>, Object> getAttachments();
	
    @Invoker
    Map<AttachmentType<?>, Object> invokeGetAttachmentMap();
	
	@Invoker
	IAttachmentHolder invokeGetExposedHolder();
}
