package com.monkey.mcbot.sdk.model;

import java.util.Set;
import java.util.UUID;

public record BotSnapshotResponse(
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
        String source,
        boolean autoTarget,
        double autoTargetRange,
        boolean respectWorldGuardPvp,
        boolean stayAfterOwnerDeath,
        boolean idleWander,
        double idleWanderRadius,
        double idleReturnDistance,
        long idleReturnDelayMs,
        boolean crystalPvp,
        boolean explosions,
        boolean enderPearls,
        boolean killMessageEnabled
) {
}
