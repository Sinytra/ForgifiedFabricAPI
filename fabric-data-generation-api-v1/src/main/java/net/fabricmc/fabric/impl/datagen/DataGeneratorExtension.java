package net.fabricmc.fabric.impl.datagen;

import java.nio.file.Path;

import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataOutput;
import net.minecraft.util.Identifier;

import net.minecraftforge.forgespi.language.IModInfo;

public interface DataGeneratorExtension {
	DataGenerator.Pack createPack(String name, DataOutput output);
	
	Pair<DataGenerator.Pack, Path> createBuiltinResourcePack(boolean shouldRun, Identifier packName, IModInfo modInfo, boolean strictValidation);
}
