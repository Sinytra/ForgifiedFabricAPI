package net.fabricmc.fabric.test.block;

import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.fabricmc.fabric.api.block.v1.FabricBlockState;

@GameTestHolder(BlockApiTestImpl.MODID)
public class BlockApiTest {

    @GameTest(templateNamespace = BlockApiTestImpl.MODID, templateName = "empty")
    @PrefixGameTestTemplate(false)
    public void testBlockAppearance(TestContext context) {
        BlockPos basePos = new BlockPos(0, 1, 0);

        context.setBlockState(basePos, BlockApiTestImpl.EXAMPLE_BLOCK.get());

        context.checkBlockState(basePos, state -> {
			FabricBlockState fabricBlockState = state;
            BlockState appearance = fabricBlockState.getAppearance(context.getWorld(), basePos, Direction.NORTH, null, null);
            return appearance.isOf(Blocks.IRON_BLOCK);
        }, () -> "Appearance of exampleblock state does not match Blocks.IRON_BLOCK");

        context.checkBlockState(basePos, state -> {
            FabricBlock block = state.getBlock(); 
            BlockState appearance = block.getAppearance(state, context.getWorld(), basePos, Direction.NORTH, null, null);
            return appearance.isOf(Blocks.IRON_BLOCK);
        }, () -> "Appearance of exampleblock block does not match Blocks.IRON_BLOCK");

        context.complete();
    }
}
