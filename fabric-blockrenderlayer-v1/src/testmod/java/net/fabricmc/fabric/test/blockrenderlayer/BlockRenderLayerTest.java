package net.fabricmc.fabric.test.blockrenderlayer;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(BlockRenderLayerTest.MODID)
public class BlockRenderLayerTest {
    public static final String MODID = "fabric_blockrenderlayer_v1_testmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);

    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL)));
    public static final RegistryObject<Fluid> EXAMPLE_FLUID = FLUIDS.register("example_item", EmptyFluid::new);

    public BlockRenderLayerTest() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onClientSetup);
        BLOCKS.register(bus);
        FLUIDS.register(bus);

        ServerLifecycleEvents.SERVER_STARTED.register(client -> {
            if (!ItemBlockRenderTypes.getRenderLayers(EXAMPLE_BLOCK.get().defaultBlockState()).contains(RenderType.cutout())) {
                throw new AssertionError("Expected EXAMPLE_BLOCK to contain RenderType.cutout render type");
            }
            if (ItemBlockRenderTypes.getRenderLayer(EXAMPLE_FLUID.get().defaultFluidState()) != RenderType.translucent()) {
                throw new AssertionError("Expected render type of EXAMPLE_FLUID to be RenderType.translucent");
            }

            // Success!
            LOGGER.info("The tests for block render type layers passed!");
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        BlockRenderLayerMap.INSTANCE.putBlock(EXAMPLE_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putFluid(EXAMPLE_FLUID.get(), RenderType.translucent());
    }
}
