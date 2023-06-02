package net.fabricmc.fabric.test.command;

import net.fabricmc.fabric.test.command.client.ClientCommandTest;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("fabric_command_api_v2_testmod")
public class CommandTestModImpl {
    
    public CommandTestModImpl() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientCommandTest.onInitializeClient();
        }
        CommandTest.onInitialize();
        CustomArgumentTest.onInitialize();
    }
}
