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

package net.fabricmc.fabric.impl.resource.loader;

import com.mojang.datafixers.util.Either;
import cpw.mods.niofs.union.UnionFileSystemProvider;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.resource.PathPackResources;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;

public class ModNioResourcePack extends PathPackResources implements ModResourcePack {
	private static final FileSystem DEFAULT_FS = FileSystems.getDefault();
	private static final UnionFileSystemProvider UFSP = (UnionFileSystemProvider) FileSystemProvider.installedProviders().stream().filter(fsp->fsp.getScheme().equals("union")).findFirst().orElseThrow(()->new IllegalStateException("Couldn't find UnionFileSystemProvider"));

	private final Either<ModMetadata, IModInfo> modMetadata;
	private final ResourcePackActivationType activationType;

	public static ModNioResourcePack create(String id, Either<ModContainer, IModInfo> mod, String subPath, PackType type, ResourcePackActivationType activationType) {
		List<Path> rootPaths = mod.map(ModContainer::getRootPaths, modInfo -> List.of(modInfo.getOwningFile().getFile().findResource(".")));
		List<Path> paths;

		if (subPath == null) {
			paths = rootPaths;
		} else {
			paths = new ArrayList<>(rootPaths.size());

			for (Path path : rootPaths) {
				path = path.toAbsolutePath().normalize();
				Path childPath = path.resolve(subPath.replace("/", path.getFileSystem().getSeparator())).normalize();

				if (!childPath.startsWith(path) || !exists(childPath)) {
					continue;
				}

				paths.add(childPath);
			}
		}

		if (paths.isEmpty()) return null;

		Path union = paths.size() == 1 ? paths.get(0) : UFSP.newFileSystem((a, b) -> true, paths.toArray(Path[]::new)).getRoot();
		ModNioResourcePack ret = new ModNioResourcePack(id, mod.mapLeft(ModContainer::getMetadata), union, activationType);

		return ret.getNamespaces(type).isEmpty() ? null : ret;
	}

	private ModNioResourcePack(String id, Either<ModMetadata, IModInfo> modInfo, Path path, ResourcePackActivationType activationType) {
		super(id, false, path);
		this.modMetadata = modInfo;
		this.activationType = activationType;
	}

	@Override
	public ModMetadata getFabricModMetadata() {
		return modMetadata.left().orElse(null);
	}

	@Override
	public IModInfo getForgeModMetadata() {
		return modMetadata.right().orElse(null);
	}

	public ResourcePackActivationType getActivationType() {
		return this.activationType;
	}

	private static boolean exists(Path path) {
		// NIO Files.exists is notoriously slow when checking the file system
		return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path);
	}
}
