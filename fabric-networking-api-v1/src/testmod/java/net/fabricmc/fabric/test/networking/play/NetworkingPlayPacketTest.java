/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.test.networking.play;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class NetworkingPlayPacketTest {
    public static final ResourceLocation TEST_CHANNEL = NetworkingTestmods.id("test_channel");

    public static void sendToTestChannel(ServerPlayer player, String stuff) {
        ServerPlayNetworking.send(player, new OverlayPacket(Component.literal(stuff)));
        NetworkingTestmods.LOGGER.info("Sent custom payload packet in {}", TEST_CHANNEL);
    }

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        NetworkingTestmods.LOGGER.info("Registering test command");

        dispatcher.register(literal("networktestcommand")
            .then(argument("stuff", string()).executes(ctx -> {
                String stuff = StringArgumentType.getString(ctx, "stuff");
                sendToTestChannel(ctx.getSource().getPlayer(), stuff);
                return Command.SINGLE_SUCCESS;
            }))
            .then(literal("bundled").executes(ctx -> {
                FriendlyByteBuf buf1 = PacketByteBufs.create();
                buf1.writeComponent(Component.literal("bundled #1"));
                FriendlyByteBuf buf2 = PacketByteBufs.create();
                buf2.writeComponent(Component.literal("bundled #2"));

                ClientboundBundlePacket packet = new ClientboundBundlePacket(List.of(
                    ServerPlayNetworking.createS2CPacket(TEST_CHANNEL, buf1),
                    ServerPlayNetworking.createS2CPacket(TEST_CHANNEL, buf2)));
                ctx.getSource().getPlayer().connection.send(packet);
                return Command.SINGLE_SUCCESS;
            })));
    }

    public static void onInitialize() {
        NetworkingTestmods.LOGGER.info("Hello from networking user!");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            NetworkingPlayPacketTest.registerCommand(dispatcher);
        });
    }

    public record OverlayPacket(Component message) implements FabricPacket {
        public static final PacketType<OverlayPacket> PACKET_TYPE = PacketType.create(TEST_CHANNEL, OverlayPacket::new);

        public OverlayPacket(FriendlyByteBuf buf) {
            this(buf.readComponent());
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeComponent(this.message);
        }

        @Override
        public PacketType<?> getType() {
            return PACKET_TYPE;
        }
    }
}
