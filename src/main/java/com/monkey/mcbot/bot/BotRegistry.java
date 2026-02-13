package com.monkey.mcbot.bot;

import com.monkey.mcbot.bot.ai.fakeplayer.FakeOfflinePlayer;
import com.monkey.mcbot.bot.ai.TrainingBot;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class BotRegistry {

    private final Map<UUID, TrainingBot> spawnedBots = new ConcurrentHashMap<>();

    public void registerBot(UUID playerUUID, TrainingBot bot) {
        spawnedBots.put(playerUUID, bot);
    }

    public boolean isBotSpawned(UUID playerUUID) {
        return spawnedBots.containsKey(playerUUID);
    }

    public UUID getBotUUID(UUID playerUUID) {
        TrainingBot bot = spawnedBots.get(playerUUID);
        if (bot == null) return null;
        return bot.getUUID();
    }

    public TrainingBot computeIfPresent(UUID playerUUID, BiFunction<UUID, TrainingBot, TrainingBot> remappingFunction) {
        return spawnedBots.computeIfPresent(playerUUID, remappingFunction);
    }

    public void removeBot(UUID playerUUID) {
        spawnedBots.remove(playerUUID);
    }

    public void removeBotByUUID(UUID botUUID) {
        spawnedBots.entrySet().removeIf(entry -> entry.getValue().getUUID().equals(botUUID));
    }

    public Map<UUID, TrainingBot> getAllBots() {
        return new HashMap<>(spawnedBots);
    }

    public TrainingBot getBot(UUID ownerUUID) {
        return spawnedBots.get(ownerUUID);
    }

    public void clear() {
        spawnedBots.clear();
    }

    public @Nullable FakeOfflinePlayer getFakeOfflinePlayer(UUID uuid) {
        TrainingBot bot = spawnedBots.get(uuid);
        if (bot == null) return null;
        return new FakeOfflinePlayer(bot);
    }
}
