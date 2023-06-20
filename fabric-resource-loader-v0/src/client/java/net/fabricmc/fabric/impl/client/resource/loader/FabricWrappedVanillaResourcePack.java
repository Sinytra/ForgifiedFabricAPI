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

package net.fabricmc.fabric.impl.client.resource.loader;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Represents a vanilla built-in resource pack with support for modded content.
 *
 * <p>Vanilla resources are provided as usual through the original resource pack,
 * all other resources will be searched for in the provided modded resource packs.</p>
 */
public class FabricWrappedVanillaResourcePack extends GroupResourcePack {
	private final AbstractPackResources originalResourcePack;

	public FabricWrappedVanillaResourcePack(AbstractPackResources originalResourcePack, List<ModResourcePack> modResourcePacks) {
		super(PackType.CLIENT_RESOURCES, modResourcePacks);
		this.originalResourcePack = originalResourcePack;
	}

	@Override
	public IoSupplier<InputStream> getRootResource(String... pathSegments) {
		FileUtil.validatePath(pathSegments);

		return this.originalResourcePack.getRootResource(String.join("/", pathSegments));
	}

	@Override
	public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
		IoSupplier<InputStream> originalPackData = this.originalResourcePack.getResource(type, id);

		if (originalPackData != null) {
			return originalPackData;
		}

		return super.getResource(type, id);
	}

	@Override
	public void listResources(PackType type, String namespace, String prefix, PackResources.ResourceOutput consumer) {
		super.listResources(type, namespace, prefix, consumer);
		this.originalResourcePack.listResources(type, namespace, prefix, consumer);
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		Set<String> namespaces = this.originalResourcePack.getNamespaces(type);

		namespaces.addAll(super.getNamespaces(type));

		return namespaces;
	}

	@Override
	public <T> @Nullable T getMetadataSection(MetadataSectionSerializer<T> metaReader) throws IOException {
		return this.originalResourcePack.getMetadataSection(metaReader);
	}

	@Override
	public String packId() {
		return this.originalResourcePack.packId();
	}

	@Override
	public void close() {
		this.originalResourcePack.close();
		super.close();
	}
}
