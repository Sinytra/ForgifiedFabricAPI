package net.fabricmc.fabric.impl.client.event.lifecycle;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientChunkEventHooks {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ClientLevel clientLevel) {
            ClientChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(clientLevel, (LevelChunk) event.getChunk());
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel clientLevel) {
            ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(clientLevel, (LevelChunk) event.getChunk());
        }
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(event.getRegistryAccess(), true);
        }
    }

    private ClientChunkEventHooks() {}
}
