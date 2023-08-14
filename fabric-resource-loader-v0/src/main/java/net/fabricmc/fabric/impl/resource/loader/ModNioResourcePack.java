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

import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.niofs.union.UnionFileSystemProvider;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceType;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class ModNioResourcePack extends PathPackResources implements ModResourcePack {
	private static final FileSystem DEFAULT_FS = FileSystems.getDefault();
	private static final UnionFileSystemProvider UFSP = (UnionFileSystemProvider) FileSystemProvider.installedProviders().stream().filter(fsp -> fsp.getScheme().equals("union")).findFirst().orElseThrow(() -> new IllegalStateException("Couldn't find UnionFileSystemProvider"));

	private final ModMetadata modInfo;
	private final ResourcePackActivationType activationType;
	private final ResourceType type;

	public static ModNioResourcePack create(String id, ModContainer mod, String subPath, ResourceType type, ResourcePackActivationType activationType) {
		List<Path> rootPaths = mod.getRootPaths();
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
		ModNioResourcePack ret = new ModNioResourcePack(id, mod.getMetadata(), union, type, activationType);

		return ret.getNamespaces(type).isEmpty() ? null : ret;
	}

	private ModNioResourcePack(String id, ModMetadata modInfo, Path path, ResourceType type, ResourcePackActivationType activationType) {
		super(id, false, path);
		this.modInfo = modInfo;
		this.type = type;
		this.activationType = activationType;
	}

	@Override
	public ModMetadata getFabricModMetadata() {
		return modInfo;
	}

	public ResourcePackActivationType getActivationType() {
		return this.activationType;
	}

	private InputSupplier<InputStream> openFile(String filename) {
		final Path path = resolve(filename);
        if (Files.exists(path)) {
			return InputSupplier.create(path);
        }
		if (ModResourcePackUtil.containsDefault(this.modInfo, filename)) {
			return () -> ModResourcePackUtil.openDefault(this.modInfo, this.type, filename);
		}
		return null;
	}

	@Nullable
	@Override
	public InputSupplier<InputStream> openRoot(String... pathSegments) {
		return openFile(String.join("/", pathSegments));
	}

	private static boolean exists(Path path) {
		// NIO Files.exists is notoriously slow when checking the file system
		return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path);
	}
}
