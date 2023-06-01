package net.fabricmc.fabric.impl.client.event.lifecycle;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MinecraftEventHooks {

    @SubscribeEvent
    public static void onClientLevelTick(TickEvent.LevelTickEvent event) {
        if (event.level instanceof ClientLevel clientLevel) {
            if (event.phase == TickEvent.Phase.START) {
                ClientTickEvents.START_WORLD_TICK.invoker().onStartTick(clientLevel);
            } else if (event.phase == TickEvent.Phase.END) {
                ClientTickEvents.END_WORLD_TICK.invoker().onEndTick(clientLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ClientTickEvents.START_CLIENT_TICK.invoker().onStartTick(Minecraft.getInstance());
        } else if (event.phase == TickEvent.Phase.END) {
            ClientTickEvents.END_CLIENT_TICK.invoker().onEndTick(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onShutdown(GameShuttingDownEvent event) {
        ClientLifecycleEvents.CLIENT_STOPPING.invoker().onClientStopping(Minecraft.getInstance());
    }

    private MinecraftEventHooks() {}
}
