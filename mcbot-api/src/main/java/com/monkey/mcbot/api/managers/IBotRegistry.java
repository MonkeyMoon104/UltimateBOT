package com.monkey.mcbot.api.managers;

import com.monkey.mcbot.api.model.BotSnapshot;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IBotRegistry {

    Map<UUID, BotSnapshot> getAllBots();

    Optional<BotSnapshot> getBot(UUID ownerUUID);

    Optional<UUID> getBotUUID(UUID ownerUUID);

    boolean isBotSpawned(UUID ownerUUID);

    int size();
}
