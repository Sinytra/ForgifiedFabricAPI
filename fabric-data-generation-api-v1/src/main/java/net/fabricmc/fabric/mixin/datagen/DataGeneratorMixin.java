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

package net.fabricmc.fabric.mixin.datagen;

import java.nio.file.Path;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.minecraftforge.forgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataOutput;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.datagen.DataGeneratorExtension;

@Mixin(DataGenerator.class)
public class DataGeneratorMixin implements DataGeneratorExtension {
	@Shadow
	private DataOutput output;

	@Override
	public DataGenerator.Pack createPack(String name, DataOutput output) {
		DataGenerator generator = (DataGenerator) (Object) this;
		return generator.new Pack(true, name, output);
	}

	@Override
	public Pair<DataGenerator.Pack, Path> createBuiltinResourcePack(boolean shouldRun, Identifier packName, IModInfo modInfo, boolean strictValidation) {
		Path path = this.output.getPath().resolve("resourcepacks").resolve(packName.getPath());
		DataGenerator generator = (DataGenerator) (Object) this;
		return Pair.of(generator.new Pack(shouldRun, packName.toString(), new FabricDataOutput(modInfo, path, strictValidation)), path);
	}
}
