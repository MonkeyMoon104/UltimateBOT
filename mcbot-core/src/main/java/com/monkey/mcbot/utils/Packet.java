package com.monkey.mcbot.utils;

import com.mojang.authlib.GameProfile;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class Packet {

    public static void sendAddPlayerPacket(Player viewer, ITrainingBot bot) {
        GameProfile profile = bot.asPlayer().getGameProfile();

        ClientboundPlayerInfoUpdatePacket packet = NMSBridgeManager.get().createAddPlayerPacket(
                bot.asPlayer().getUUID(), profile, NMSBridgeManager.get().getProfileName(profile)
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

        byte yHeadRot = packDegrees(bot.asPlayer().getYHeadRot());
        byte yBodyRot = packDegrees(bot.asPlayer().getYRot());
        byte xRot = packDegrees(bot.asPlayer().getXRot());

        handle.connection.send(new ClientboundRotateHeadPacket(bot.asPlayer(), yHeadRot));
        handle.connection.send(new ClientboundMoveEntityPacket.Rot(
                bot.asPlayer().getId(),
                yBodyRot,
                xRot,
                bot.asPlayer().onGround()
        ));

        List<SynchedEntityData.DataValue<?>> metadata = bot.asPlayer().getEntityData().getNonDefaultValues();
        if (metadata != null && !metadata.isEmpty()) {
            handle.connection.send(new ClientboundSetEntityDataPacket(bot.asPlayer().getId(), metadata));
        }
    }

    private static byte packDegrees(float value) {
        return (byte) (value * 256.0F / 360.0F);
    }
}
