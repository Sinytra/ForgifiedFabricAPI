package net.fabricmc.fabric.test.model.loading;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_model_loading_api_v1_testmod")
public class ModelTestMod {
	public ModelTestMod() {
		if (FMLLoader.getDist().isClient()) {
			ModelTestModClient.onInitializeClient();
			NestedModelLoadingTest.onInitializeClient();
			PreparablePluginTest.onInitializeClient();
		}
	}
}
