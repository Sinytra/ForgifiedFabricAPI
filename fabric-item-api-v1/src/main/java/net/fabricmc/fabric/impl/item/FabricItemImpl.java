package net.fabricmc.fabric.impl.item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.sinytra.fabric.item_api.generated.GeneratedEntryPoint;

@Mod(GeneratedEntryPoint.MOD_ID)
public class FabricItemImpl {

	public FabricItemImpl(IEventBus bus) {
		bus.addListener(ItemComponentTooltipProviderRegistryImpl::register);
	}
}
