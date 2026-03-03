package com.monkey.mcbot.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.server.level.ClientInformation;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class BotFactory {

    public static GameProfile createProfile(Player viewer, UUID botUUID, String botName) {
        GameProfile viewerProfile = ((CraftPlayer) viewer).getProfile();
        Collection<Property> textures = viewerProfile.getProperties().get("textures");

        GameProfile profile = new GameProfile(botUUID, botName);
        if (!textures.isEmpty()) {
            profile.getProperties().put("textures", textures.iterator().next());
        }
        return profile;
    }

    public static ClientInformation createClientInformation() {
        return NMSBridgeManager.get().createClientInformation();
    }
}