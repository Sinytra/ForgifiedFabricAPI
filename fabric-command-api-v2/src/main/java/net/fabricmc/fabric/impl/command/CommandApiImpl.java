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

package net.fabricmc.fabric.impl.command;

import java.util.HashMap;
import java.util.Map;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.RegisterEvent;

import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;

@Mod(CommandApiImpl.MODID)
public class CommandApiImpl {
    public static final String MODID = "fabric_command_api_v2";

    @SuppressWarnings("rawtypes")
    private static final Map<Class, ArgumentSerializer<?, ?>> ARGUMENT_TYPE_CLASSES = new HashMap<>();
    private static final Map<Identifier, ArgumentSerializer<?, ?>> ARGUMENT_TYPES = new HashMap<>();

    public static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void registerArgumentType(Identifier id, Class<? extends A> clazz, ArgumentSerializer<A, T> serializer) {
        ARGUMENT_TYPE_CLASSES.put(clazz, serializer);
        ARGUMENT_TYPES.put(id, serializer);
    }

    public CommandApiImpl() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CommandApiImpl::registerArgumentTypes);
        MinecraftForge.EVENT_BUS.addListener(CommandApiImpl::registerCommands);
        if (FMLLoader.getDist() == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener(ClientCommandInternals::registerClientCommands);
        }
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        CommandRegistrationCallback.EVENT.invoker().register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    private static void registerArgumentTypes(RegisterEvent event) {
        event.register(RegistryKeys.COMMAND_ARGUMENT_TYPE, helper -> {
            ARGUMENT_TYPE_CLASSES.forEach(ArgumentTypes::registerByClass);
            ARGUMENT_TYPES.forEach(helper::register);
        });
    }
}
