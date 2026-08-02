package com.monkey.ultimatebot.api.model;

import com.monkey.ultimatebot.common.util.TextValues;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Immutable runtime snapshot of a managed bot.
 *
 * @param ownerUUID owner UUID associated with this bot slot
 * @param botUUID runtime bot entity UUID, may be null when unavailable
 * @param botType bot type label
 * @param botDifficulty current difficulty label
 * @param minDifficultyLevel minimum allowed difficulty label
 * @param maxDifficultyLevel maximum allowed difficulty label
 * @param follow current follow state
 * @param combat current combat state
 * @param totemCount current totem count
 * @param minTotemCount minimum allowed totems
 * @param maxTotemCount maximum allowed totems
 * @param targetUUID primary target UUID, when available
 * @param targetUUIDs full target set (for multi-target modes)
 * @param source origin of bot creation (core/api)
 * @param attackBots whether this bot can target other managed bots
 * @param targetMode categories of living entities this bot may attack
 * @param explosionBlockDamage whether bot explosions may destroy terrain
 */
public record BotSnapshot(
        UUID ownerUUID,
        @Nullable UUID botUUID,
        String botType,
        String botDifficulty,
        String minDifficultyLevel,
        String maxDifficultyLevel,
        boolean follow,
        boolean combat,
        int totemCount,
        int minTotemCount,
        int maxTotemCount,
        @Nullable UUID targetUUID,
        Set<UUID> targetUUIDs,
        BotSource source,
        boolean autoTarget,
        double autoTargetRange,
        boolean attackBots,
        BotTargetMode targetMode,
        boolean respectWorldGuardPvp,
        boolean stayAfterOwnerDeath,
        boolean idleWander,
        double idleWanderRadius,
        double idleReturnDistance,
        long idleReturnDelayMs,
        boolean crystalPvp,
        boolean explosions,
        boolean explosionBlockDamage,
        boolean enderPearls,
        boolean healing,
        boolean killMessageEnabled) {
    public BotSnapshot {
        botType = TextValues.orElseIfBlank(botType, "UNKNOWN");
        botDifficulty = TextValues.orElseIfBlank(botDifficulty, "UNKNOWN");
        minDifficultyLevel = TextValues.orElseIfBlank(minDifficultyLevel, "EASY");
        maxDifficultyLevel = TextValues.orElseIfBlank(maxDifficultyLevel, "GOD");
        targetUUIDs = targetUUIDs == null ? Set.of() : Set.copyOf(targetUUIDs);
        source = source == null ? BotSource.CORE : source;
        targetMode = targetMode == null ? BotTargetMode.PLAYERS : targetMode;
    }

    /**
     * Indicates whether at least one target is currently assigned.
     *
     * @return {@code true} when primary target exists or target set is not empty
     */
    public boolean hasTarget() {
        return targetUUID != null || !targetUUIDs.isEmpty();
    }

    /**
     * Returns the runtime entity UUID or fails when this is a pre-spawn snapshot.
     *
     * @return available runtime bot UUID
     * @throws NullPointerException when the bot entity is not available yet
     */
    public UUID requireBotUUID() {
        return Objects.requireNonNull(botUUID, "botUUID is unavailable for this snapshot");
    }
}
