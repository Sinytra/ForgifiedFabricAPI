package net.fabricmc.fabric.mixin.client.rendering;

import java.util.List;

import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.client.gui.GuiLayerManager.NamedLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiLayerManager.class)
public interface GuiLayerManagerAccessor {
	@Accessor
	List<NamedLayer> getLayers();
}
