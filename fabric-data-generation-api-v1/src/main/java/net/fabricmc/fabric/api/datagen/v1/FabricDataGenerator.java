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

package net.fabricmc.fabric.api.datagen.v1;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

import net.fabricmc.loader.api.ModContainer;

import net.minecraft.registry.RegistryBuilder;

import net.minecraft.util.Util;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.datagen.DataGeneratorExtension;

/**
 * An extension to vanilla's {@link DataGenerator} providing mod specific data, and helper functions.
 */
public final class FabricDataGenerator extends DataGenerator {
	private final IModInfo modContainer;
	private final boolean strictValidation;
	private final FabricDataOutput fabricOutput;
	private final CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture;
	private final DataGenerator parent;

	public static FabricDataGenerator create(String modid, GatherDataEvent event) {
		return create(modid, event, event.getLookupProvider());
	}

	public static FabricDataGenerator create(String modid, GatherDataEvent event, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
		IModInfo modInfo = ModList.get().getModContainerById(modid).orElseThrow().getModInfo();
		return create(modInfo, event, registries);
	}

	public static FabricDataGenerator create(IModInfo modInfo, GatherDataEvent event, RegistryBuilder registryBuilder) {
		CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture = event.getLookupProvider().thenApply(r -> FabricDataGenHelper.createRegistryWrapper(r, registryBuilder));
		return create(modInfo, event, registriesFuture);
	}

	private static FabricDataGenerator create(IModInfo modInfo, GatherDataEvent event, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
		final DataGenerator dataGenerator = event.getGenerator();
		return new FabricDataGenerator(dataGenerator, dataGenerator.getPackOutput().getPath(), modInfo, FabricDataGenHelper.STRICT_VALIDATION, registries);
	}

	@ApiStatus.Internal
	public FabricDataGenerator(DataGenerator parent, Path output, IModInfo mod, boolean strictValidation, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, SharedConstants.getGameVersion(), true);
		this.parent = parent;
		this.modContainer = Objects.requireNonNull(mod);
		this.strictValidation = strictValidation;
		this.fabricOutput = new FabricDataOutput(mod, output, strictValidation);
		this.registriesFuture = registriesFuture;
	}

	/**
	 * Create a default {@link Pack} instance for generating a mod's data.
	 */
	public Pack createPack() {
		DataGenerator.Pack parentPack = ((DataGeneratorExtension) this.parent).createPack(modContainer.getDisplayName(), this.fabricOutput);
		return new Pack(parentPack);
	}

	/**
	 * Create a new {@link Pack} instance for generating a builtin resource pack.
	 *
	 * <p>To be used in conjunction with {@link net.fabricmc.fabric.api.resource.ResourceManagerHelper#registerBuiltinResourcePack}
	 *
	 * <p>The path in which the resource pack is generated is {@code "resourcepacks/<id path>"}. {@code id path} being the path specified
	 * in the identifier.
	 */
	public Pack createBuiltinResourcePack(Identifier id) {
		Pair<DataGenerator.Pack, Path> parentPack = ((DataGeneratorExtension) this.parent).createBuiltinResourcePack(true, id, modContainer, strictValidation);
		return new Pack(parentPack.getFirst());
	}

	/**
	 * Returns the {@link ModContainer} for the mod that this data generator has been created for.
	 *
	 * @return a {@link ModContainer} instance
	 */
	public ModContainer getModContainer() {
		throw new UnsupportedOperationException("Unavailable on MinecraftForge, call getForgeModContainer instead");
	}

	/**
	 * Returns the {@link IModInfo} for the mod that this data generator has been created for.
	 *
	 * @return a {@link IModInfo} instance
	 */
	public IModInfo getForgeModContainer() {
		return modContainer;
	}

	/**
	 * Returns the mod ID for the mod that this data generator has been created for.
	 *
	 * @return a mod ID
	 */
	public String getModId() {
		return getForgeModContainer().getModId();
	}

	/**
	 * When enabled data providers can do strict validation to ensure that all entries have data generated for them.
	 *
	 * @return if strict validation should be enabled
	 */
	public boolean isStrictValidationEnabled() {
		return strictValidation;
	}

	/**
	 * @deprecated Please use {@link FabricDataGenerator#createPack()}
	 */
	@Override
	@Deprecated
	public DataGenerator.Pack createVanillaPack(boolean shouldRun) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated Please use {@link FabricDataGenerator#createBuiltinResourcePack(Identifier)}
	 */
	@Override
	@Deprecated
	public DataGenerator.Pack createVanillaSubPack(boolean shouldRun, String packName) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Represents a pack of generated data (i.e. data pack or resource pack). Providers are added to a pack.
	 */
	public final class Pack extends DataGenerator.Pack {
		private final DataGenerator.Pack parent;
		
		private Pack(DataGenerator.Pack parent) {
			super(false, null, null);
			this.parent = parent;
		}

		/**
		 * Registers a constructor of {@link DataProvider} which takes a {@link FabricDataOutput}.
		 *
		 * @return the {@link DataProvider}
		 */
		public <T extends DataProvider> T addProvider(Factory<T> factory) {
			return this.parent.addProvider(output -> factory.create((FabricDataOutput) output));
		}

		/**
		 * Registers a constructor of {@link DataProvider} which takes a {@link FabricDataOutput} and the registries.
		 * This is used, for example, with {@link net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider}.
		 *
		 * @return the {@link DataProvider}
		 */
		public <T extends DataProvider> T addProvider(RegistryDependentFactory<T> factory) {
			return this.parent.addProvider(output -> factory.create((FabricDataOutput) output, registriesFuture));
		}

		@Override
		public <T extends DataProvider> T addProvider(DataProvider.Factory<T> factory) {
			return this.parent.addProvider(factory);
		}

		/**
		 * A factory of a data provider. This is usually the constructor.
		 */
		@FunctionalInterface
		public interface Factory<T extends DataProvider> {
			T create(FabricDataOutput output);
		}

		/**
		 * A factory of a data provider. This is usually the constructor.
		 * The provider has access to the registries.
		 */
		@FunctionalInterface
		public interface RegistryDependentFactory<T extends DataProvider> {
			T create(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture);
		}
	}
}
