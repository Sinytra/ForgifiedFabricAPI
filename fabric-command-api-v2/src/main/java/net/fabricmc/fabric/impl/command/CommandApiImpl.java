package net.fabricmc.fabric.impl.command;

import com.mojang.brigadier.arguments.ArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.RegisterEvent;

import java.util.HashMap;
import java.util.Map;

@Mod(CommandApiImpl.MODID)
public class CommandApiImpl {
    public static final String MODID = "fabric_command_api_v2";

    @SuppressWarnings("rawtypes")
    private static final Map<Class, ArgumentTypeInfo<?, ?>> ARGUMENT_TYPE_CLASSES = new HashMap<>();
    private static final Map<ResourceLocation, ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = new HashMap<>();

    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgumentType(ResourceLocation id, Class<A> clazz, ArgumentTypeInfo<A, T> serializer) {
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
        event.register(Registries.COMMAND_ARGUMENT_TYPE, helper -> {
            ARGUMENT_TYPE_CLASSES.forEach(ArgumentTypeInfos::registerByClass);
            ARGUMENT_TYPES.forEach(helper::register);
        });
    }
}
