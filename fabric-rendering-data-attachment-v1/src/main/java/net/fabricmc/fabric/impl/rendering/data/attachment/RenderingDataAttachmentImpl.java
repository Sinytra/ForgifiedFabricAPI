package net.fabricmc.fabric.impl.rendering.data.attachment;

import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_rendering_data_attachment_v1")
public class RenderingDataAttachmentImpl {
    public static final ModelProperty<Object> MODEL_RENDER_DATA_ATTACHMENT = new ModelProperty<>();
    
    public RenderingDataAttachmentImpl() {
    }
}
