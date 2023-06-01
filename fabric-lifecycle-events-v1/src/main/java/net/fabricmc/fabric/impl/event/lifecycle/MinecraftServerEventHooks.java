package net.fabricmc.fabric.impl.event.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MinecraftServerEventHooks {

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.level instanceof ServerLevel serverLevel) {
            if (event.phase == TickEvent.Phase.START) {
                ServerTickEvents.START_WORLD_TICK.invoker().onStartTick(serverLevel);
            } else if (event.phase == TickEvent.Phase.END) {
                ServerTickEvents.END_WORLD_TICK.invoker().onEndTick(serverLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ServerTickEvents.START_SERVER_TICK.invoker().onStartTick(event.getServer());
        } else if (event.phase == TickEvent.Phase.END) {
            ServerTickEvents.END_SERVER_TICK.invoker().onEndTick(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerAboutToStartEvent event) {
        ServerLifecycleEvents.SERVER_STARTING.invoker().onServerStarting(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLifecycleEvents.SERVER_STARTED.invoker().onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLifecycleEvents.SERVER_STOPPING.invoker().onServerStopping(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ServerLifecycleEvents.SERVER_STOPPED.invoker().onServerStopped(event.getServer());
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ServerWorldEvents.LOAD.invoker().onWorldLoad(serverLevel.getServer(), serverLevel);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(serverLevel.getServer(), serverLevel);
        }
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(event.getRegistryAccess(), false);
        }
    }

    private MinecraftServerEventHooks() {}
}
