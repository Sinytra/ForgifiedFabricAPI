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

package net.fabricmc.fabric.mixin.networking;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.buffer.ByteBuf;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationPacketListenerImpl;
import net.fabricmc.fabric.impl.networking.FabricRegistryFriendlyByteBuf;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ServerConfigurationPacketListenerImpl.class, priority = 900)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImpl implements PacketListenerExtensions, FabricServerConfigurationPacketListenerImpl {
	@Shadow
	protected abstract void finishCurrentTask(ConfigurationTask.Type key);

	@Shadow
	@Final
	private Queue<ConfigurationTask> configurationTasks;

	@Unique
	private ServerConfigurationNetworkAddon addon;

	public ServerConfigurationPacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie arg) {
		super(server, connection, arg);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ServerConfigurationNetworkAddon((ServerConfigurationPacketListenerImpl) (Object) this, this.server);
		// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
		this.addon.lateInit();
	}
	
	@ModifyExpressionValue(method = "startConfiguration", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/registration/NetworkRegistry;getInitialListeningChannels(Lnet/minecraft/network/protocol/PacketFlow;)Ljava/util/Set;"))
	private Set<Identifier> addInitialReceivableChannels(Set<Identifier> original) {
		Set<Identifier> union = new HashSet<>(original);
		union.addAll(this.addon.getReceivableChannels());
		return union;
	}

	@Inject(method = "runConfiguration", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/ConfigurationInitialization;configureEarlyTasks(Lnet/minecraft/network/protocol/configuration/ServerConfigurationPacketListener;Ljava/util/function/Consumer;)V"))
	private void beforeConfigureEarlyTasks(CallbackInfo ci) {
		this.addon.preConfiguration();
	}

	@Override
	public ServerConfigurationNetworkAddon getAddon() {
		return addon;
	}

	@Override
	public void addTask(ConfigurationTask task) {
		configurationTasks.add(task);
	}

	@Override
	public void completeTask(ConfigurationTask.Type key) {
		finishCurrentTask(key);
	}

	@WrapOperation(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;decorator(Lnet/minecraft/core/RegistryAccess;Lnet/neoforged/neoforge/network/connection/ConnectionType;)Ljava/util/function/Function;"))
	private Function<ByteBuf, RegistryFriendlyByteBuf> bindChannelInfo(RegistryAccess registryManager, ConnectionType connectionType, Operation<Function<ByteBuf, RegistryFriendlyByteBuf>> original) {
		return original.call(registryManager, connectionType).andThen(registryByteBuf -> {
			FabricRegistryFriendlyByteBuf fabricRegistryFriendlyByteBuf = (FabricRegistryFriendlyByteBuf) registryByteBuf;
			fabricRegistryFriendlyByteBuf.fabric_setSendableConfigurationChannels(Set.copyOf(addon.getSendableChannels()));
			return registryByteBuf;
		});
	}
}
