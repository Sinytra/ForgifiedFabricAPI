package net.fabricmc.fabric.impl.base.registry;

public interface EarlyRegistry {
	void gatherCallbacks();

	void applyCallbacks();
}
