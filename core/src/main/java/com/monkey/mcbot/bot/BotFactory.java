package com.monkey.mcbot.bot;

import com.mojang.authlib.GameProfile;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.server.level.ClientInformation;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BotFactory {

    public static GameProfile createProfile(Player viewer, UUID botUUID, String botName) {
        return NMSBridgeManager.get().copyProfileWithTextures(viewer, botUUID, botName);
    }

    public static ClientInformation createClientInformation() {
        return NMSBridgeManager.get().createClientInformation();
    }
}