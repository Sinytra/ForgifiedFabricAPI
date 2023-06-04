package net.fabricmc.fabric.impl.dimension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class SimpleTeleporter implements ITeleporter {
    private final PortalInfo target;
    private final ITeleporter wrapped;

    public SimpleTeleporter(PortalInfo target, ITeleporter wrapped) {
        this.target = target;
        this.wrapped = wrapped;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        // Disable end-specific behavior
        return repositionEntity.apply(false);
    }

    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return this.target;
    }

    @Override
    public boolean isVanilla() {
        return this.wrapped.getClass() == PortalForcer.class;
    }
}
