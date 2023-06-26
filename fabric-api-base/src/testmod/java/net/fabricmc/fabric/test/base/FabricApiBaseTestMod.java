package net.fabricmc.fabric.test.base;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_api_base_testmod")
public class FabricApiBaseTestMod {
	public FabricApiBaseTestMod() {
		FabricApiBaseTestInit.onInitialize();
		if (FMLLoader.getDist() == Dist.DEDICATED_SERVER) {
			FabricApiAutoTestServer.onInitializeServer();
		}
	}
}
