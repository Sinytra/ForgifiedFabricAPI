package net.fabricmc.fabric.impl.event.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerListHooks {

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.invoker().onSyncDataPackContents(event.getPlayer(), true);
        } else {
            for (ServerPlayer serverPlayer : event.getPlayerList().getPlayers()) {
                ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.invoker().onSyncDataPackContents(serverPlayer, false);
            }
        }
    }

    private PlayerListHooks() {}
}
