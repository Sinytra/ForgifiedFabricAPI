package net.fabricmc.fabric.test.block;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.fabricmc.fabric.api.block.v1.FabricBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BlockApiTestImpl.MODID)
public class BlockApiTest {

    @GameTest(templateNamespace = BlockApiTestImpl.MODID, template = "empty")
    @PrefixGameTestTemplate(false)
    public void testBlockAppearance(GameTestHelper context) {
        BlockPos basePos = new BlockPos(0, 1, 0);

        context.setBlock(basePos, BlockApiTestImpl.EXAMPLE_BLOCK.get());

        context.assertBlockState(basePos, state -> {
            FabricBlockState fabricBlockState = (FabricBlockState) state;
            BlockState appearance = fabricBlockState.getAppearance(context.getLevel(), basePos, Direction.NORTH, null, null);
            return appearance.is(Blocks.IRON_BLOCK);
        }, () -> "Appearance of exampleblock state does not match Blocks.IRON_BLOCK");

        context.assertBlockState(basePos, state -> {
            FabricBlock block = (FabricBlock) state.getBlock(); 
            BlockState appearance = block.getAppearance(state, context.getLevel(), basePos, Direction.NORTH, null, null);
            return appearance.is(Blocks.IRON_BLOCK);
        }, () -> "Appearance of exampleblock block does not match Blocks.IRON_BLOCK");

        context.succeed();
    }
}
