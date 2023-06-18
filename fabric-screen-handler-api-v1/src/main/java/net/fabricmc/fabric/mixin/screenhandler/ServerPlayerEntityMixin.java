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

package net.fabricmc.fabric.mixin.screenhandler;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.FabricScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {

	private ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Redirect(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"))
	private void fabric_closeHandledScreenIfAllowed(ServerPlayer player, MenuProvider factory) {
		if (((FabricScreenHandlerFactory) factory).shouldCloseCurrentScreen()) {
			this.closeContainer();
		} else {
			// Called by closeHandledScreen in vanilla
			this.doCloseContainer();
		}
	}

	@Inject(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;nextContainerCounter()V"), cancellable = true)
	private void fabric_replaceVanillaScreenPacket(MenuProvider factory, CallbackInfoReturnable<OptionalInt> cir) {
		if (factory instanceof SimpleMenuProvider simpleFactory && ((SimpleMenuProviderAccessor) (Object) simpleFactory).getMenuConstructor() instanceof ExtendedScreenHandlerFactory extendedFactory) {
			factory = extendedFactory;
		}

		if (factory instanceof ExtendedScreenHandlerFactory extendedFactory) {
			// Redirect to forge
			NetworkHooks.openScreen((ServerPlayer) (Object) this, extendedFactory);

			AbstractContainerMenu handler = Objects.requireNonNull(containerMenu);
			if (handler.getType() instanceof ExtendedScreenHandlerType<?>) {
				cir.setReturnValue(OptionalInt.of(((ServerPlayer) (Object) this).containerCounter));
			} else {
				ResourceLocation id = ForgeRegistries.MENU_TYPES.getKey(handler.getType());
				throw new IllegalArgumentException("[Fabric] Non-extended screen handler " + id + " must not be opened with an ExtendedScreenHandlerFactory!");
			}
		}
	}
}
