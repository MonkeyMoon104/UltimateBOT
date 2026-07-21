package com.monkey.mcbot.api.model;

import java.util.Set;
import java.util.UUID;

/**
 * Immutable runtime snapshot of a managed bot.
 *
 * @param ownerUUID owner UUID associated with this bot slot
 * @param botUUID runtime bot entity UUID, may be null when unavailable
 * @param botType bot type label
 * @param botRank current rank label
 * @param minBotRank minimum allowed rank label
 * @param maxBotRank maximum allowed rank label
 * @param follow current follow state
 * @param combat current combat state
 * @param totemCount current totem count
 * @param minTotemCount minimum allowed totems
 * @param maxTotemCount maximum allowed totems
 * @param targetUUID primary target UUID, when available
 * @param targetUUIDs full target set (for multi-target modes)
 * @param source origin of bot creation (core/api)
 */
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
        BotSource source,
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
    public BotSnapshot {
        botType = botType == null || botType.isBlank() ? "UNKNOWN" : botType;
        botRank = botRank == null || botRank.isBlank() ? "UNKNOWN" : botRank;
        minBotRank = minBotRank == null || minBotRank.isBlank() ? "EASY" : minBotRank;
        maxBotRank = maxBotRank == null || maxBotRank.isBlank() ? "GOD" : maxBotRank;
        targetUUIDs = targetUUIDs == null ? Set.of() : Set.copyOf(targetUUIDs);
        source = source == null ? BotSource.CORE : source;
    }

    /**
     * Indicates whether at least one target is currently assigned.
     *
     * @return {@code true} when primary target exists or target set is not empty
     */
    public boolean hasTarget() {
        return targetUUID != null || !targetUUIDs.isEmpty();
    }
}
