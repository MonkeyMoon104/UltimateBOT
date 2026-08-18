package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.util.Collections;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public class BotFactory {

    public static BotProfileData createProfile(Player viewer, UUID botUUID, String botName) {
        return NMSBridgeManager.get().copyProfileWithTextures(viewer, botUUID, botName);
    }

    public static BotProfileData createRandomProfile(UUID botUUID, String botName) {
        return new BotProfileData(botUUID, botName, Collections.emptyList());
    }

    public static BotProfileData createProfileWithTexture(
            UUID botUUID, String botName, @Nullable String textureValue, @Nullable String textureSignature) {
        if (textureValue == null || textureValue.trim().isEmpty()) {
            return createRandomProfile(botUUID, botName);
        }
        return NMSBridgeManager.get().createProfileWithTexture(botUUID, botName, textureValue, textureSignature);
    }
}
