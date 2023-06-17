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

package net.fabricmc.fabric.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftClientMixin {
	@Shadow
	public Screen screen;

	@Unique
	private Screen tickingScreen;

	@Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V", shift = At.Shift.AFTER))
	private void onScreenRemove(@Nullable Screen screen, CallbackInfo ci) {
		ScreenEvents.remove(this.screen).invoker().onRemove(this.screen);
	}

	@Inject(method = "destroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V", shift = At.Shift.AFTER))
	private void onScreenRemoveBecauseStopping(CallbackInfo ci) {
		ScreenEvents.remove(this.screen).invoker().onRemove(this.screen);
	}

	// Synthetic method in `tick`
	// These two injections should be caught by "Screen#wrapScreenError" if anything fails in an event and then rethrown in the crash report
	@Inject(method = {"m_231445_", "lambda$tick$40()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;tick()V"), require = 1, remap = false)
	private void beforeScreenTick(CallbackInfo ci) {
		// Store the screen in a variable in case someone tries to change the screen during this before tick event.
		// If someone changes the screen, the after tick event will likely have class cast exceptions or an NPE.
		this.tickingScreen = this.screen;
		ScreenEvents.beforeTick(this.tickingScreen).invoker().beforeTick(this.tickingScreen);
	}

	// Synthetic method in `tick`
	@Inject(method = {"m_231445_", "lambda$tick$40()V"}, at = @At("TAIL"), require = 1, remap = false)
	private void afterScreenTick(CallbackInfo ci) {
		ScreenEvents.afterTick(this.tickingScreen).invoker().afterTick(this.tickingScreen);
		// Finally set the currently ticking screen to null
		this.tickingScreen = null;
	}

	// The LevelLoadingScreen is the odd screen that isn't ticked by the main tick loop, so we fire events for this screen.
	// We Coerce the package-private inner class representing the world load action so we don't need an access widener.
	@Inject(method = "doWorldLoad(Ljava/lang/String;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/WorldStem;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LevelLoadingScreen;tick()V"))
	private void beforeLoadingScreenTick(CallbackInfo ci) {
		// Store the screen in a variable in case someone tries to change the screen during this before tick event.
		// If someone changes the screen, the after tick event will likely have class cast exceptions or throw a NPE.
		this.tickingScreen = this.screen;
		ScreenEvents.beforeTick(this.tickingScreen).invoker().beforeTick(this.tickingScreen);
	}

	@Inject(method = "doWorldLoad(Ljava/lang/String;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/WorldStem;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runTick(Z)V"))
	private void afterLoadingScreenTick(CallbackInfo ci) {
		ScreenEvents.afterTick(this.tickingScreen).invoker().afterTick(this.tickingScreen);
		// Finally set the currently ticking screen to null
		this.tickingScreen = null;
	}
}
