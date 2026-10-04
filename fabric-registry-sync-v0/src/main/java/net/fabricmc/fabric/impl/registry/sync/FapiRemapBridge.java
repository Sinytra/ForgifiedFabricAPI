package net.fabricmc.fabric.impl.registry.sync;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import net.neoforged.neoforge.registries.callback.ClearCallback;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;

public final class FapiRemapBridge<T> implements ClearCallback<T>, BakeCallback<T> {
	@Nullable
	private Int2ObjectMap<Identifier> oldIdMap;

	@Override
	public void onClear(Registry<T> registry, boolean full) {
		if (full) {
			oldIdMap = null;
			return;
		}
		oldIdMap = new Int2ObjectOpenHashMap<>();
		for (T value : registry) {
			oldIdMap.put(registry.getId(value), registry.getKey(value));
		}
	}

	@Override
	public void onBake(Registry<T> registry) {
		if (oldIdMap == null) {
			return;
		}
		Int2ObjectMap<Identifier> old = oldIdMap;
		oldIdMap = null;

		Object2IntMap<Identifier> newByKey = new Object2IntOpenHashMap<>();
		newByKey.defaultReturnValue(Integer.MIN_VALUE);
		for (T value : registry) {
			newByKey.put(registry.getKey(value), registry.getId(value));
		}

		Int2IntMap rawIdChangeMap = new Int2IntOpenHashMap();
		for (var e : old.int2ObjectEntrySet()) {
			int newId = newByKey.getInt(e.getValue());
			if (newId != Integer.MIN_VALUE) {
				rawIdChangeMap.put(e.getIntKey(), newId);
			}
		}

		RegistryIdRemapCallback.RemapState<T> state = new RemapStateImpl<>(registry, old, rawIdChangeMap);
		RegistryIdRemapCallback.event(registry).invoker().onRemap(state);
	}
}
