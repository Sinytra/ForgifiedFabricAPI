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

import java.util.Objects;
import java.util.OptionalInt;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

	private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Shadow
	public abstract void closeHandledScreen();

	@Redirect(method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;closeHandledScreen()V"))
	private void fabric_closeHandledScreenIfAllowed(ServerPlayerEntity player, NamedScreenHandlerFactory factory) {
		if (factory.shouldCloseCurrentScreen()) {
			this.closeHandledScreen();
		} else {
			// Called by closeHandledScreen in vanilla
			this.onHandledScreenClosed();
		}
	}

	@Inject(method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;incrementScreenHandlerSyncId()V"), cancellable = true)
	private void fabric_replaceVanillaScreenPacket(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir) {
		if (factory instanceof SimpleNamedScreenHandlerFactory simpleFactory && ((SimpleNamedScreenHandlerFactoryAccessor) (Object) simpleFactory).getBaseFactory() instanceof ExtendedScreenHandlerFactory extendedFactory) {
			factory = extendedFactory;
		}

		if (factory instanceof ExtendedScreenHandlerFactory extendedFactory) {
			// Redirect to forge
			NetworkHooks.openScreen((ServerPlayerEntity) (Object) this, extendedFactory);

			ScreenHandler handler = Objects.requireNonNull(currentScreenHandler);
			if (handler.getType() instanceof ExtendedScreenHandlerType<?>) {
				cir.setReturnValue(OptionalInt.of(((ServerPlayerEntity) (Object) this).screenHandlerSyncId));
			}
			else {
				Identifier id = ForgeRegistries.MENU_TYPES.getKey(handler.getType());
				throw new IllegalArgumentException("[Fabric] Non-extended screen handler " + id + " must not be opened with an ExtendedScreenHandlerFactory!");
			}
		}
	}
}
