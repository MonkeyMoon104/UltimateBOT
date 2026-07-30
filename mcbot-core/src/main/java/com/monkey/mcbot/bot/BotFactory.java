package com.monkey.mcbot.bot;

import com.mojang.authlib.GameProfile;
import com.monkey.mcbot.nms.NMSBridgeManager;
import java.util.UUID;
import net.minecraft.server.level.ClientInformation;
import org.bukkit.entity.Player;

public class BotFactory {

    public static GameProfile createProfile(Player viewer, UUID botUUID, String botName) {
        return NMSBridgeManager.get().copyProfileWithTextures(viewer, botUUID, botName);
    }

    public static GameProfile createRandomProfile(UUID botUUID, String botName) {
        return new GameProfile(botUUID, botName);
    }

    public static GameProfile createProfileWithTexture(
            UUID botUUID, String botName, String textureValue, String textureSignature) {
        if (textureValue == null || textureValue.isBlank()) {
            return createRandomProfile(botUUID, botName);
        }
        return NMSBridgeManager.get().createProfileWithTexture(botUUID, botName, textureValue, textureSignature);
    }

    public static ClientInformation createClientInformation() {
        return NMSBridgeManager.get().createClientInformation();
    }
}
