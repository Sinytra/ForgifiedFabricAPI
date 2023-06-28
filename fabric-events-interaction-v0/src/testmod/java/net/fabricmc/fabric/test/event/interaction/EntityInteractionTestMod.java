package net.fabricmc.fabric.test.event.interaction;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

import net.fabricmc.fabric.test.client.event.interaction.ClientPreAttackTests;

@Mod(EntityInteractionTestMod.MODID)
public class EntityInteractionTestMod {
	public static final String MODID = "fabric_events_interaction_v0_testmod";

	public EntityInteractionTestMod() {
		if (FMLLoader.getDist() == Dist.CLIENT) {
			ClientPreAttackTests.onInitializeClient();
		}
		AttackBlockTests.onInitialize();
		PlayerBreakBlockTests.onInitialize();
		PlayerPickBlockTests.onInitialize();
		UseEntityTests.onInitialize();
	}
}
