package com.monkey.mcbot.sdk.model;

import java.util.Set;
import java.util.UUID;

/**
 * Immutable snapshot of a bot returned by the remote API.
 *
 * @param ownerUUID primary owner UUID when the bot has one
 * @param botUUID spawned bot entity UUID
 * @param botType selected armor/combat type
 * @param botRank selected rank profile
 * @param minBotRank minimum random rank profile
 * @param maxBotRank maximum random rank profile
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
 * @param enderPearls whether ender pearl logic is enabled
 * @param healing whether healing logic is enabled
 * @param killMessageEnabled whether the built-in kill message is enabled
 */
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
        boolean enderPearls,
        boolean healing,
        boolean killMessageEnabled
) {
    /**
     * Returns whether the bot currently has any target configured.
     *
     * @return whether a target is present
     */
    public boolean hasTarget() {
        return targetUUID != null || targetUUIDs != null && !targetUUIDs.isEmpty();
    }

    /**
     * Returns whether the bot has multiple configured targets.
     *
     * @return whether more than one target UUID is configured
     */
    public boolean hasMultipleTargets() {
        return targetUUIDs != null && targetUUIDs.size() > 1;
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
     * Returns whether rank selection uses a random range.
     *
     * @return whether min and max rank differ from the selected rank
     */
    public boolean usesRankRange() {
        return botRank == null || !botRank.equalsIgnoreCase(minBotRank) || !botRank.equalsIgnoreCase(maxBotRank);
    }

}
