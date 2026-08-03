package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.fakeplayer.FakeOfflinePlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

public class BotRegistry {

    private final Map<UUID, ITrainingBot> spawnedBots = new ConcurrentHashMap<>();

    public void registerBot(UUID playerUUID, ITrainingBot bot) {
        spawnedBots.put(playerUUID, bot);
    }

    public boolean isBotSpawned(UUID playerUUID) {
        return spawnedBots.containsKey(playerUUID);
    }

    public @Nullable UUID getBotUUID(UUID playerUUID) {
        ITrainingBot bot = spawnedBots.get(playerUUID);
        if (bot == null) return null;
        return bot.asPlayer().getUUID();
    }

    public @Nullable UUID getOwnerUUIDByBotUUID(@Nullable UUID botUUID) {
        if (botUUID == null) {
            return null;
        }
        for (Map.Entry<UUID, ITrainingBot> entry : spawnedBots.entrySet()) {
            ITrainingBot bot = entry.getValue();
            if (bot != null
                    && bot.asPlayer() != null
                    && botUUID.equals(bot.asPlayer().getUUID())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public @Nullable ITrainingBot computeIfPresent(
            UUID playerUUID, BiFunction<UUID, ITrainingBot, ITrainingBot> remappingFunction) {
        return spawnedBots.computeIfPresent(playerUUID, remappingFunction);
    }

    public void removeBot(UUID playerUUID) {
        spawnedBots.remove(playerUUID);
    }

    public void removeBotByUUID(UUID botUUID) {
        spawnedBots
                .entrySet()
                .removeIf(entry -> entry.getValue().asPlayer().getUUID().equals(botUUID));
    }

    public Map<UUID, ITrainingBot> getAllBots() {
        return new HashMap<>(spawnedBots);
    }

    public int size() {
        return spawnedBots.size();
    }

    public @Nullable ITrainingBot getBot(UUID ownerUUID) {
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
