package net.fabricmc.fabric.impl.resource.loader;

import com.mojang.datafixers.util.Either;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.List;

public final class SafeApiHandler {
	static List<Path> getFabricModContainerPaths(Either<ModContainer, IModInfo> container) {
		return container.map(ModContainer::getRootPaths, modInfo -> List.of(modInfo.getOwningFile().getFile().findResource(".")));
	}

	static Either<ModMetadata, IModInfo> getFabricModContainerMetadata(Either<ModContainer, IModInfo> container) {
		return container.mapLeft(ModContainer::getMetadata);
	}
}
