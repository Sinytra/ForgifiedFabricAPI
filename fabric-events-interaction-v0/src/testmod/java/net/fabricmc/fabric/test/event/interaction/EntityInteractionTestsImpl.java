package net.fabricmc.fabric.test.event.interaction;

import net.fabricmc.fabric.test.client.event.interaction.ClientPreAttackTests;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(EntityInteractionTestsImpl.MODID)
public class EntityInteractionTestsImpl {
    public static final String MODID = "fabric_events_interaction_v0_testmod";

    public EntityInteractionTestsImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientPreAttackTests.onInitializeClient();
        }
        AttackBlockTests.onInitialize();
        PlayerBreakBlockTests.onInitialize();
        PlayerPickBlockTests.onInitialize();
        UseEntityTests.onInitialize();
    }
}
