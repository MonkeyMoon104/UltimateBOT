package com.monkey.mcbot.utils;

import com.mojang.authlib.GameProfile;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Packet {

    public static void sendAddPlayerPacket(Player viewer, ITrainingBot bot) {
        GameProfile profile = bot.asPlayer().getGameProfile();

        ClientboundPlayerInfoUpdatePacket packet = NMSBridgeManager.get().createAddPlayerPacket(
                bot.asPlayer().getUUID(), profile, profile.getName()
        );

        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        handle.connection.send(packet);
    }

    public static void sendSpawnPlayerPacket(Player viewer, ITrainingBot bot) {
        ClientboundAddEntityPacket spawnPacket = NMSBridgeManager.get().createSpawnPlayerPacket(
                bot.asPlayer().getId(), bot.asPlayer().getUUID(),
                bot.asPlayer().getX(), bot.asPlayer().getY(), bot.asPlayer().getZ(),
                bot.asPlayer().getXRot(), bot.asPlayer().getYRot(), bot.asPlayer().getYHeadRot()
        );

        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        handle.connection.send(spawnPacket);
    }
}