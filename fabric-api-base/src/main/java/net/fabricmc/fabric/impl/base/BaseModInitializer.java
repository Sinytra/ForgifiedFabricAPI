package net.fabricmc.fabric.impl.base;

import net.fabricmc.fabric.impl.base.registry.EarlyRegistry;

import net.minecraft.core.Registry;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import org.sinytra.fabric.api_base.generated.GeneratedEntryPoint;

@Mod(GeneratedEntryPoint.MOD_ID)
public class BaseModInitializer {

	public BaseModInitializer(IEventBus bus) {
		bus.addListener(EventPriority.LOWEST, ModifyRegistriesEvent.class, e -> {
			for (Registry<?> registry : e.getRegistries()) {
				if (registry instanceof EarlyRegistry er) {
					er.applyCallbacks();
				}
			}
		});
	}
}
