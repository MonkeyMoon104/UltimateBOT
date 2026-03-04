package com.monkey.mcbot.bot;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.fakeplayer.FakeOfflinePlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class BotRegistry {

    private final Map<UUID, ITrainingBot> spawnedBots = new ConcurrentHashMap<>();

    public void registerBot(UUID playerUUID, ITrainingBot bot) {
        spawnedBots.put(playerUUID, bot);
    }

    public boolean isBotSpawned(UUID playerUUID) {
        return spawnedBots.containsKey(playerUUID);
    }

    public UUID getBotUUID(UUID playerUUID) {
        ITrainingBot bot = spawnedBots.get(playerUUID);
        if (bot == null) return null;
        return bot.asPlayer().getUUID();
    }

    public ITrainingBot computeIfPresent(UUID playerUUID, BiFunction<UUID, ITrainingBot, ITrainingBot> remappingFunction) {
        return spawnedBots.computeIfPresent(playerUUID, remappingFunction);
    }

    public void removeBot(UUID playerUUID) {
        spawnedBots.remove(playerUUID);
    }

    public void removeBotByUUID(UUID botUUID) {
        spawnedBots.entrySet().removeIf(entry -> entry.getValue().asPlayer().getUUID().equals(botUUID));
    }

    public Map<UUID, ITrainingBot> getAllBots() {
        return new HashMap<>(spawnedBots);
    }

    public ITrainingBot getBot(UUID ownerUUID) {
        return spawnedBots.get(ownerUUID);
    }

    public void clear() {
        spawnedBots.clear();
    }

    public @Nullable FakeOfflinePlayer getFakeOfflinePlayer(UUID uuid) {
        ITrainingBot bot = spawnedBots.get(uuid);
        if (bot == null) return null;
        return new FakeOfflinePlayer(bot);
    }
}
