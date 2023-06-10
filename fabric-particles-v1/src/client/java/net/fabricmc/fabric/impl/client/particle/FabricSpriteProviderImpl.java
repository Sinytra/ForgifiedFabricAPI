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

package net.fabricmc.fabric.impl.client.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.mixin.client.particle.ParticleManagerAccessor;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

import java.util.List;

public class FabricSpriteProviderImpl implements FabricSpriteProvider {
	private final ParticleEngine particleManager;
	private final SpriteSet delegate;

	FabricSpriteProviderImpl(ParticleEngine particleManager, SpriteSet delegate) {
		this.particleManager = particleManager;
		this.delegate = delegate;
	}

	@Override
	public TextureAtlas getAtlas() {
		return ((ParticleManagerAccessor) particleManager).getTextureAtlas();
	}

	@Override
	public List<TextureAtlasSprite> getSprites() {
		return ((ParticleManagerAccessor.SimpleSpriteProviderAccessor) delegate).getSprites();
	}

	@Override
	public TextureAtlasSprite get(int i, int j) {
		return delegate.get(i, j);
	}

	@Override
	public TextureAtlasSprite get(RandomSource random) {
		return delegate.get(random);
	}
}
