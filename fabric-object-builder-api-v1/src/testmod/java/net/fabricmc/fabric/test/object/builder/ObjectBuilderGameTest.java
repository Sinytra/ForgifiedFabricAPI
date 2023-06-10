/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.test.object.builder;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ObjectBuilderTestConstants.MOD_ID)
public class ObjectBuilderGameTest {
	
	@GameTest(templateNamespace = ObjectBuilderTestConstants.MOD_ID, template = "empty")
	@PrefixGameTestTemplate(false)
	public void testBlockUse(GameTestHelper context) {
		List<Block> blocks = List.of(BlockEntityTypeBuilderTest.INITIAL_BETRAYAL_BLOCK.get(), BlockEntityTypeBuilderTest.ADDED_BETRAYAL_BLOCK.get(), BlockEntityTypeBuilderTest.FIRST_MULTI_BETRAYAL_BLOCK.get(), BlockEntityTypeBuilderTest.SECOND_MULTI_BETRAYAL_BLOCK.get());
		BlockPos pos = BlockPos.ZERO;

		for (Block block : blocks) {
			context.setBlock(pos, block);
			context.useBlock(pos);
			pos = pos.above();
		}

		context.succeed();
	}
}
