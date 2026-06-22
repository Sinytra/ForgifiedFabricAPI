package net.fabricmc.fabric.impl.client.model.loading;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel.UnbakedRoot;
import net.minecraft.client.resources.model.ModelBaker;

public class FabricCustomUnbakedModel implements CustomUnbakedBlockStateModel {
	private final net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel inner;

	public FabricCustomUnbakedModel(net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel inner) {
		this.inner = inner;
	}

	public net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel getInner() {
		return inner;
	}

	@Override
	public UnbakedRoot asRoot() {
		return this.inner.asRoot();
	}

	@Override
	public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
		return CustomUnbakedBlockStateModelRegistry.wrapCodec(this.inner.codec());
	}

	@Override
	public BlockStateModel bake(ModelBaker modelBaker) {
		return this.inner.bake(modelBaker);
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		this.inner.resolveDependencies(resolver);
	}
}
