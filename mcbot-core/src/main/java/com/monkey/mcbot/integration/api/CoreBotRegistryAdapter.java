package com.monkey.mcbot.integration.api;

import com.monkey.mcbot.api.managers.IBotRegistry;
import com.monkey.mcbot.api.model.BotSnapshot;
import com.monkey.mcbot.bot.BotRegistry;
import com.monkey.mcbot.bot.ai.ITrainingBot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class CoreBotRegistryAdapter implements IBotRegistry {

    private final BotRegistry botRegistry;

    public CoreBotRegistryAdapter(BotRegistry botRegistry) {
        this.botRegistry = Objects.requireNonNull(botRegistry, "botRegistry");
    }

    @Override
    public Map<UUID, BotSnapshot> getAllBots() {
        Map<UUID, BotSnapshot> snapshots = new HashMap<>();

        for (Map.Entry<UUID, ITrainingBot> entry : botRegistry.getAllBots().entrySet()) {
            BotSnapshot snapshot = BotSnapshotMapper.toSnapshot(entry.getKey(), entry.getValue());
            if (snapshot != null) {
                snapshots.put(entry.getKey(), snapshot);
            }
        }

        return Collections.unmodifiableMap(snapshots);
    }

    @Override
    public Optional<BotSnapshot> getBot(UUID ownerUUID) {
        if (ownerUUID == null) {
            return Optional.empty();
        }

        ITrainingBot bot = botRegistry.getBot(ownerUUID);
        return Optional.ofNullable(BotSnapshotMapper.toSnapshot(ownerUUID, bot));
    }

    @Override
    public Optional<UUID> getBotUUID(UUID ownerUUID) {
        if (ownerUUID == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(botRegistry.getBotUUID(ownerUUID));
    }

    @Override
    public boolean isBotSpawned(UUID ownerUUID) {
        return ownerUUID != null && botRegistry.isBotSpawned(ownerUUID);
    }

    @Override
    public int size() {
        return botRegistry.getAllBots().size();
    }
}
