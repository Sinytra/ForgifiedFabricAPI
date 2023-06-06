package net.fabricmc.fabric.test.item;

import net.fabricmc.fabric.test.item.client.TooltipTests;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(FabricItemTestsImpl.MODID)
public class FabricItemTestsImpl {
    public static final String MODID = "fabric_item_api_v1_testmod";
    
    public FabricItemTestsImpl() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        
        if (FMLLoader.getDist() == Dist.CLIENT) {
            TooltipTests.onInitializeClient();
        }
        ArmorKnockbackResistanceTest.onInitialize(bus);
        CustomDamageTest.onInitialize(bus);
        FabricItemSettingsTests.onInitialize(bus);
        ItemUpdateAnimationTest.onInitialize(bus);
        ModifyItemAttributeModifiersCallbackTest.onInitialize();
    }
}
