package net.fabricmc.fabric.test.transfer;

import net.fabricmc.fabric.test.transfer.ingame.TransferTestInitializer;
import net.fabricmc.fabric.test.transfer.ingame.client.FluidVariantRenderTest;
import net.fabricmc.fabric.test.transfer.unittests.UnitTestsInitializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(TransferApiTests.MODID)
public class TransferApiTests {
    public static final String MODID = "fabric_transfer_api_v1_testmod";

    public TransferApiTests() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        if (FMLLoader.getDist() == Dist.CLIENT) {
            FluidVariantRenderTest.onInitializeClient();
        }
        TransferTestInitializer.onInitialize(bus);
        UnitTestsInitializer.onInitialize();
    }
}
