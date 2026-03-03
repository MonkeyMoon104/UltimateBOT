package com.monkey.mcbot.utils;

import com.mojang.authlib.GameProfile;
import com.monkey.mcbot.bot.ai.TrainingBot;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Packet {

    public static void sendAddPlayerPacket(Player viewer, TrainingBot bot) {
        GameProfile profile = bot.getGameProfile();

        ClientboundPlayerInfoUpdatePacket packet = NMSBridgeManager.get().createAddPlayerPacket(
                bot.getUUID(), profile, profile.getName()
        );

        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        handle.connection.send(packet);
    }

    public static void sendSpawnPlayerPacket(Player viewer, TrainingBot bot) {
        ClientboundAddEntityPacket spawnPacket = NMSBridgeManager.get().createSpawnPlayerPacket(
                bot.getId(), bot.getUUID(),
                bot.getX(), bot.getY(), bot.getZ(),
                bot.getXRot(), bot.getYRot(), bot.getYHeadRot()
        );

        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        handle.connection.send(spawnPacket);
    }
}