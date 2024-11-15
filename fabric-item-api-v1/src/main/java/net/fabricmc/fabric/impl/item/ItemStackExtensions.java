package net.fabricmc.fabric.impl.item;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface ItemStackExtensions {
	@Nullable LivingEntity fabric_getLivingEntity();
	void fabric_setLivingEntity(LivingEntity livingEntity);
}
