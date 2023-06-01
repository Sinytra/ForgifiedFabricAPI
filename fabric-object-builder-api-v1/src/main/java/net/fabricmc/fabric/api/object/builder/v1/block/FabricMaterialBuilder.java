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

package net.fabricmc.fabric.api.object.builder.v1.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;

public class FabricMaterialBuilder extends Material.Builder {
	public FabricMaterialBuilder(MaterialColor color) {
		super(color);
	}

	public FabricMaterialBuilder(DyeColor color) {
		this(color.getMaterialColor());
	}

	public FabricMaterialBuilder burnable() {
		super.flammable();
		return this;
	}

	public FabricMaterialBuilder pistonBehavior(PushReaction behavior) {
		this.pushReaction = behavior;
		return this;
	}

	public FabricMaterialBuilder lightPassesThrough() {
		super.notSolidBlocking();
		return this;
	}

	public FabricMaterialBuilder destroyedByPiston() {
		super.destroyOnPush();
		return this;
	}

	public FabricMaterialBuilder blocksPistons() {
		super.notPushable();
		return this;
	}

	public FabricMaterialBuilder allowsMovement() {
		super.noCollider();
		return this;
	}

	@Override
	public FabricMaterialBuilder liquid() {
		super.liquid();
		return this;
	}

	public FabricMaterialBuilder notSolid() {
		super.nonSolid();
		return this;
	}

	@Override
	public FabricMaterialBuilder replaceable() {
		super.replaceable();
		return this;
	}
}
