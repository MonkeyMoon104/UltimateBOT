package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class BotLookup {

    private final BotRegistry registry;

    public BotLookup(BotRegistry registry) {
        this.registry = java.util.Objects.requireNonNull(registry, "registry");
    }

    public @Nullable ITrainingBot getBotByOwnerUUID(UUID ownerUUID) {
        return registry.getBot(ownerUUID);
    }

    public @Nullable ITrainingBot getBotSafe(UUID ownerUUID) {
        return registry.getBot(ownerUUID);
    }

    public boolean isBotSpawned(UUID ownerUUID) {
        return registry.isBotSpawned(ownerUUID);
    }

    public void removeBot(UUID botUUID) {
        registry.removeBotByUUID(botUUID);
    }
}
