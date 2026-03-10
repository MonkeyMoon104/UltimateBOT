package com.monkey.mcbot.api.model;

import java.util.Set;
import java.util.UUID;

public record BotSnapshot(
        UUID ownerUUID,
        UUID botUUID,
        String botType,
        String botRank,
        String minBotRank,
        String maxBotRank,
        boolean follow,
        boolean combat,
        int totemCount,
        int minTotemCount,
        int maxTotemCount,
        UUID targetUUID,
        Set<UUID> targetUUIDs,
        BotSource source
) {
    public BotSnapshot {
        botType = botType == null || botType.isBlank() ? "UNKNOWN" : botType;
        botRank = botRank == null || botRank.isBlank() ? "UNKNOWN" : botRank;
        minBotRank = minBotRank == null || minBotRank.isBlank() ? "EASY" : minBotRank;
        maxBotRank = maxBotRank == null || maxBotRank.isBlank() ? "GOD" : maxBotRank;
        targetUUIDs = targetUUIDs == null ? Set.of() : Set.copyOf(targetUUIDs);
        source = source == null ? BotSource.CORE : source;
    }

    public boolean hasTarget() {
        return targetUUID != null || !targetUUIDs.isEmpty();
    }
}
