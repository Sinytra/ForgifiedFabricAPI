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

import com.google.common.base.Preconditions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_dimensions_v1")
public final class FabricDimensionInternals {

    @SuppressWarnings("unchecked")
    public static <E extends Entity> E changeDimension(E teleported, ServerLevel dimension, PortalInfo target) {
        Preconditions.checkArgument(!teleported.level.isClientSide, "Entities can only be teleported on the server side");
        Preconditions.checkArgument(Thread.currentThread() == ((ServerLevel) teleported.level).getServer().getRunningThread(), "Entities must be teleported from the main server thread");

        // Fast path for teleporting within the same dimension.
        if (teleported.getLevel() == dimension) {
            if (teleported instanceof ServerPlayer serverPlayerEntity) {
                serverPlayerEntity.connection.teleport(target.pos.x, target.pos.y, target.pos.z, target.yRot, teleported.getXRot());
            } else {
                teleported.moveTo(target.pos.x, target.pos.y, target.pos.z, target.yRot, teleported.getXRot());
            }

            teleported.setDeltaMovement(target.speed);
            teleported.setYHeadRot(target.yRot);

            return teleported;
        }
        return (E) teleported.changeDimension(dimension, new SimpleTeleporter(target, dimension.getPortalForcer()));
    }
}
