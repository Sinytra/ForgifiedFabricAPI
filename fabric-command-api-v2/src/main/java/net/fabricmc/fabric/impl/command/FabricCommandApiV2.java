package net.fabricmc.fabric.impl.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.brigadier.arguments.ArgumentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class FabricCommandApiV2 implements ModInitializer {
	@SuppressWarnings("rawtypes")
	private static final Map<Class, ArgumentTypeInfo<?, ?>> ARGUMENT_TYPE_CLASSES = new ConcurrentHashMap<>();
	private static final Map<Identifier, ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = new ConcurrentHashMap<>();

	@Override
	public void onInitialize() {
		IEventBus bus = ModLoadingContext.get().getActiveContainer().getEventBus();
		bus.addListener(RegisterEvent.class, event ->
				event.register(Registries.COMMAND_ARGUMENT_TYPE, helper -> {
					ARGUMENT_TYPE_CLASSES.forEach(ArgumentTypeInfos::registerByClass);
					ARGUMENT_TYPES.forEach(helper::register);

					ARGUMENT_TYPE_CLASSES.clear();
					ARGUMENT_TYPES.clear();
				}));
		NeoForge.EVENT_BUS.addListener(
				RegisterCommandsEvent.class,
				event -> CommandRegistrationCallback.EVENT.invoker()
						.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection())
		);
	}

	public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgumentType(Identifier id, Class<? extends A> clazz, ArgumentTypeInfo<A, T> serializer) {
		ARGUMENT_TYPE_CLASSES.put(clazz, serializer);
		ARGUMENT_TYPES.put(id, serializer);
	}
}
