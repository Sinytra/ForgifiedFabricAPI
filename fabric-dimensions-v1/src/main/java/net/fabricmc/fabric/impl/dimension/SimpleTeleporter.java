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

package net.fabricmc.fabric.impl.dimension;

import java.util.function.Function;

import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.TeleportTarget;

public class SimpleTeleporter implements ITeleporter {
    private final TeleportTarget target;
    private final ITeleporter wrapped;

    public SimpleTeleporter(TeleportTarget target, ITeleporter wrapped) {
        this.target = target;
        this.wrapped = wrapped;
    }

	@Override
	public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
		// Disable end-specific behavior
		return repositionEntity.apply(false);
	}

	@Override
	public @Nullable TeleportTarget getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld, TeleportTarget> defaultPortalInfo) {
		return target;
	}

    @Override
    public boolean isVanilla() {
        return this.wrapped.getClass() == PortalForcer.class;
    }
}
