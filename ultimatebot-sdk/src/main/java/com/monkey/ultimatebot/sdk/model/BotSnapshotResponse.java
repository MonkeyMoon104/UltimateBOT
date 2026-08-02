package com.monkey.ultimatebot.sdk.model;

import com.monkey.ultimatebot.common.util.TextValues;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Immutable snapshot of a bot returned by the remote API.
 *
 * @param ownerUUID primary owner UUID when the bot has one
 * @param botUUID spawned bot entity UUID
 * @param botType selected armor/combat type
 * @param botDifficulty selected difficulty profile
 * @param minDifficultyLevel minimum random difficulty profile
 * @param maxDifficultyLevel maximum random difficulty profile
 * @param follow whether follow mode is active
 * @param combat whether combat mode is active
 * @param totemCount selected totem count
 * @param minTotemCount minimum random totem count
 * @param maxTotemCount maximum random totem count
 * @param targetUUID current single target UUID
 * @param targetUUIDs configured target UUIDs
 * @param source creation source
 * @param autoTarget whether automatic target acquisition is enabled
 * @param autoTargetRange target acquisition range
 * @param attackBots whether this bot can target other managed bots
 * @param targetMode categories of living entities this bot may attack
 * @param respectWorldGuardPvp whether PvP-disabled WorldGuard regions are avoided
 * @param stayAfterOwnerDeath whether the bot remains after owner death
 * @param idleWander whether idle wandering is enabled
 * @param idleWanderRadius idle wandering radius
 * @param idleReturnDistance distance that triggers return to spawn
 * @param idleReturnDelayMs delay before returning to spawn
 * @param crystalPvp whether crystal PvP logic is enabled
 * @param explosions whether explosive combat is enabled
 * @param explosionBlockDamage whether bot explosions may destroy terrain
 * @param enderPearls whether ender pearl logic is enabled
 * @param healing whether healing logic is enabled
 * @param killMessageEnabled whether the built-in kill message is enabled
 */
public record BotSnapshotResponse(
        @Nullable UUID ownerUUID,
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
        String source,
        boolean autoTarget,
        double autoTargetRange,
        boolean attackBots,
        SdkBotTargetMode targetMode,
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
    public BotSnapshotResponse {
        botType = TextValues.orElseIfBlank(botType, "UNKNOWN");
        botDifficulty = TextValues.orElseIfBlank(botDifficulty, "UNKNOWN");
        minDifficultyLevel = TextValues.orElseIfBlank(minDifficultyLevel, "EASY");
        maxDifficultyLevel = TextValues.orElseIfBlank(maxDifficultyLevel, "GOD");
        targetUUIDs = targetUUIDs == null ? Set.of() : Set.copyOf(targetUUIDs);
        source = TextValues.orElseIfBlank(source, "CORE");
        targetMode = targetMode == null ? SdkBotTargetMode.PLAYERS : targetMode;
    }

    /**
     * Returns whether the bot currently has any target configured.
     *
     * @return whether a target is present
     */
    public boolean hasTarget() {
        return targetUUID != null || !targetUUIDs.isEmpty();
    }

    /**
     * Returns whether the bot has multiple configured targets.
     *
     * @return whether more than one target UUID is configured
     */
    public boolean hasMultipleTargets() {
        return targetUUIDs.size() > 1;
    }

    /**
     * Returns whether the bot was created through a public API path.
     *
     * @return whether the creation source is API
     */
    public boolean isApiCreated() {
        return "API".equalsIgnoreCase(source);
    }

    /**
     * Returns whether crystal or other explosive logic can run.
     *
     * @return whether explosive combat is currently possible
     */
    public boolean explosiveCombatEnabled() {
        return combat && explosions && crystalPvp;
    }

    /**
     * Returns whether difficulty selection uses a random range.
     *
     * @return whether min and max difficulty differ from the selected difficulty
     */
    public boolean usesDifficultyRange() {
        return !botDifficulty.equalsIgnoreCase(minDifficultyLevel)
                || !botDifficulty.equalsIgnoreCase(maxDifficultyLevel);
    }
}
