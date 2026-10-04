package net.fabricmc.fabric.mixin.client.rendering;

import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.client.gui.GuiLayerManager.NamedLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GuiLayerManager.class)
public interface GuiLayerManagerAccessor {
	@Accessor
	List<NamedLayer> getLayers();
}
