package com.monkey.ultimatebot.utils;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import org.bukkit.entity.Player;

public final class Packet {

    private Packet() {}

    public static void sendAddPlayerPacket(Player viewer, ITrainingBot bot) {
        NMSBridgeManager.get().sendTabListAdd(viewer, bot);
    }

    public static void sendRemovePlayerPacket(Player viewer, ITrainingBot bot) {
        NMSBridgeManager.get().sendTabListRemove(viewer, bot);
    }

    public static void sendSpawnPlayerPacket(Player viewer, ITrainingBot bot) {
        NMSBridgeManager.get().sendSpawnAndMeta(viewer, bot);
    }
}
