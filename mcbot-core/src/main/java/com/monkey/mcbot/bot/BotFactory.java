package com.monkey.mcbot.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.server.level.ClientInformation;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BotFactory {

    public static GameProfile createProfile(Player viewer, UUID botUUID, String botName) {
        return NMSBridgeManager.get().copyProfileWithTextures(viewer, botUUID, botName);
    }

    public static GameProfile createRandomProfile(UUID botUUID, String botName) {
        return new GameProfile(botUUID, botName);
    }

    public static GameProfile createProfileWithTexture(UUID botUUID,
                                                       String botName,
                                                       String textureValue,
                                                       String textureSignature) {
        GameProfile profile = new GameProfile(botUUID, botName);
        if (textureValue == null || textureValue.isBlank()) {
            return profile;
        }

        Property property = textureSignature == null || textureSignature.isBlank()
                ? new Property("textures", textureValue)
                : new Property("textures", textureValue, textureSignature);
        profile.getProperties().put("textures", property);
        return profile;
    }

    public static ClientInformation createClientInformation() {
        return NMSBridgeManager.get().createClientInformation();
    }
}
