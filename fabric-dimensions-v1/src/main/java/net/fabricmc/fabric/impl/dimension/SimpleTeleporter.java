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
