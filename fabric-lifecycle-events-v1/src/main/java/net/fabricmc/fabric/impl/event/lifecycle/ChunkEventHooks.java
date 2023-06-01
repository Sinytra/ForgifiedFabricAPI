package net.fabricmc.fabric.impl.event.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ChunkEventHooks {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(serverLevel, (LevelChunk) event.getChunk());
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(serverLevel, (LevelChunk) event.getChunk());
        }
    }

    private ChunkEventHooks() {}
}
