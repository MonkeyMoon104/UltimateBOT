package it.coralmc.sandbox.bot.util.registry;

import java.util.*;

public class BotRegistry {

    private static final Map<UUID, UUID> spawnedBots = new HashMap<>();

    public static void registerBot(UUID playerUUID, UUID botUUID) {
        spawnedBots.put(playerUUID, botUUID);
    }

    public static boolean isBotSpawned(UUID playerUUID) {
        return spawnedBots.containsKey(playerUUID);
    }

    public static UUID getBotUUID(UUID playerUUID) {
        return spawnedBots.get(playerUUID);
    }

    public static void removeBot(UUID playerUUID) {
        spawnedBots.remove(playerUUID);
    }

    public static void removeBotByUUID(UUID botUUID) {
        spawnedBots.entrySet().removeIf(entry -> entry.getValue().equals(botUUID));
    }

    public static Map<UUID, UUID> getAllBots() {
        return new HashMap<>(spawnedBots);
    }

    public static void clear() {
        spawnedBots.clear();
    }
}