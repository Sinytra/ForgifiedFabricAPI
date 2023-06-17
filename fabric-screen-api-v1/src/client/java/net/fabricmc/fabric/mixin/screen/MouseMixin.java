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

import net.fabricmc.fabric.impl.client.screen.MouseExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
abstract class MouseMixin implements MouseExtensions {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private Double horizontalScrollAmount;

	@Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onScreenMouseScrollPre(Lnet/minecraft/client/MouseHandler;Lnet/minecraft/client/gui/screens/Screen;D)Z"))
	private void beforeMouseScrollEvent(long window, double horizontal, double vertical, CallbackInfo ci) {
		// Apply same calculations to horizontal scroll as vertical scroll amount has
		this.horizontalScrollAmount = this.minecraft.options.discreteMouseScroll().get() ? Math.signum(horizontal) : horizontal * this.minecraft.options.mouseWheelSensitivity().get();
	}

	@Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onScreenMouseScrollPost(Lnet/minecraft/client/MouseHandler;Lnet/minecraft/client/gui/screens/Screen;D)V", shift = At.Shift.AFTER))
	private void afterMouseScrollEvent(long window, double horizontal, double vertical, CallbackInfo ci) {
		this.horizontalScrollAmount = null;
	}

	@Override
	public double getHorizontalScroll() {
		return horizontalScrollAmount;
	}
}
