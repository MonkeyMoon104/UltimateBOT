package com.monkey.ultimatebot.integration.api;

import com.monkey.ultimatebot.api.managers.IBotRegistry;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.BotRegistry;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.*;

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
