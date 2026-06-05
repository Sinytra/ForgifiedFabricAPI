package net.fabricmc.fabric.impl.base.registry;

import net.minecraft.resources.ResourceKey;

public record AddData<T>(int id, ResourceKey<T> key, T value) {
}
