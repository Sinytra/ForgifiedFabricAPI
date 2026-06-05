package net.fabricmc.fabric.mixin.base;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.neoforge.registries.BaseMappedRegistry;
import net.neoforged.neoforge.registries.callback.AddCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.impl.base.registry.AddData;
import net.fabricmc.fabric.impl.base.registry.EarlyRegistry;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> extends BaseMappedRegistry<T> implements EarlyRegistry {
	@Unique
	private List<AddData<T>> fabric_bufferedAddCallbacks = new ArrayList<>();
	@Unique
	private boolean fabric_gatherCallbacks;

	@Override
	public void gatherCallbacks() {
		fabric_gatherCallbacks = true;
	}

	@Override
	public void applyCallbacks() {
		for (AddData<T> data : fabric_bufferedAddCallbacks) {
			for (AddCallback<T> callback : this.addCallbacks) {
				callback.onAdd(this, data.id(), data.key(), data.value());
			}
		}
		
		fabric_gatherCallbacks = false;
	}

	@Inject(
			method = "register(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"
			)
	)
	private void beforeOnAddCallback(int id, ResourceKey<T> key, T value, RegistrationInfo registrationInfo, CallbackInfoReturnable<Holder.Reference<T>> cir) {
		if (this.fabric_gatherCallbacks && this.addCallbacks.isEmpty()) {
			this.fabric_bufferedAddCallbacks.add(new AddData<>(id, key, value));
		}
	}
}
