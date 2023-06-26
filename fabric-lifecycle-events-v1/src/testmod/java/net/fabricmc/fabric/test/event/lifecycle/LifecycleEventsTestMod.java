package net.fabricmc.fabric.test.event.lifecycle;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

import net.fabricmc.fabric.test.event.lifecycle.client.ClientBlockEntityLifecycleTests;
import net.fabricmc.fabric.test.event.lifecycle.client.ClientEntityLifecycleTests;
import net.fabricmc.fabric.test.event.lifecycle.client.ClientLifecycleTests;
import net.fabricmc.fabric.test.event.lifecycle.client.ClientTickTests;

@Mod("fabric_lifecycle_events_v1_testmod")
public class LifecycleEventsTestMod {

	public LifecycleEventsTestMod() {
		CommonLifecycleTests.onInitialize();
		new ServerBlockEntityLifecycleTests().onInitialize();
		new ServerEntityLifecycleTests().onInitialize();
		new ServerLifecycleTests().onInitialize();
		new ServerResourceReloadTests().onInitialize();
		new ServerTickTests().onInitialize();
		if (FMLLoader.getDist() == Dist.CLIENT) {
			new ClientBlockEntityLifecycleTests().onInitializeClient();
			new ClientEntityLifecycleTests().onInitializeClient();
			new ClientLifecycleTests().onInitializeClient();
			new ClientTickTests().onInitializeClient();
		}
	}
}
