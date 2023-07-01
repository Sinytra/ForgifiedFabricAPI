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
