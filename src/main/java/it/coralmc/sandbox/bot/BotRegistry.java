package it.coralmc.sandbox.bot;

import it.coralmc.sandbox.bot.ai.crazy.FakeOfflinePlayer;
import it.coralmc.sandbox.bot.ai.TrainingBot;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BotRegistry {

    private final Map<UUID, TrainingBot> spawnedBots = new HashMap<>();

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

    public void removeBot(UUID playerUUID) {
        spawnedBots.remove(playerUUID);
    }

    public void removeBotByUUID(UUID botUUID) {
        spawnedBots.entrySet().removeIf(entry -> entry.getValue().getUUID().equals(botUUID));
    }

    public Map<UUID, TrainingBot> getAllBots() {
        return new HashMap<>(spawnedBots);
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
