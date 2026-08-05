package com.monkey.ultimatebot.sdk.model.response;

import com.monkey.ultimatebot.common.model.BlastProtectionSettings;
import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.BotMode;
import com.monkey.ultimatebot.common.model.BotSource;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.common.model.BrainKey;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import com.monkey.ultimatebot.common.util.TextValues;
import com.monkey.ultimatebot.sdk.model.request.BotEquipmentSlotRequest;
import com.monkey.ultimatebot.sdk.model.request.BotLocationRequest;
import com.monkey.ultimatebot.sdk.model.type.SdkBotEquipmentSlot;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Immutable snapshot of a bot returned by the remote API.
 *
 * @param ownerUUID primary owner UUID when the bot has one
 * @param botUUID spawned bot entity UUID
 * @param botMode lifecycle and ownership mode
 * @param difficulty selected difficulty profile
 * @param minDifficulty minimum random difficulty profile
 * @param maxDifficulty maximum random difficulty profile
 * @param combatMode selected combat mode
 * @param combatTuning resolved combat tuning
 * @param customizedCombatTuning whether the tuning is an override
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
 * @param teamOwnerUUIDs complete owner set for a shared team bot
 * @param botNameTemplate configured bot name template
 * @param botSkinSource configured skin source
 * @param minArmor minimum configured armor tier
 * @param maxArmor maximum configured armor tier
 * @param armor current armor tier
 * @param blastProtection per-piece blast-protection state
 * @param changeableFollow whether GUI follow changes are allowed
 * @param changeableCombat whether GUI combat changes are allowed
 * @param changeableBlast whether GUI blast changes are allowed
 * @param changeableArmor whether GUI armor changes are allowed
 * @param changeableTotem whether GUI totem changes are allowed
 * @param changeableDifficulty whether GUI difficulty changes are allowed
 * @param changeableCombatMode whether GUI combat-mode changes are allowed
 * @param spawnLocation configured spawn location, when explicit
 * @param killMessage configured custom kill message, when present
 * @param equipmentSlots persistent equipment-slot overrides
 */
public record BotSnapshotResponse(
        @Nullable UUID ownerUUID,
        @Nullable UUID botUUID,
        BotMode botMode,
        DifficultyTier difficulty,
        DifficultyTier minDifficulty,
        DifficultyTier maxDifficulty,
        CombatMode combatMode,
        @Nullable BrainKey brain,
        CombatTuning combatTuning,
        boolean customizedCombatTuning,
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
        boolean killMessageEnabled,
        Set<UUID> teamOwnerUUIDs,
        String botNameTemplate,
        String botSkinSource,
        BotArmorTier minArmor,
        BotArmorTier maxArmor,
        BotArmorTier armor,
        BlastProtectionSettings blastProtection,
        boolean changeableFollow,
        boolean changeableCombat,
        boolean changeableBlast,
        boolean changeableArmor,
        boolean changeableTotem,
        boolean changeableDifficulty,
        boolean changeableCombatMode,
        @Nullable BotLocationRequest spawnLocation,
        @Nullable String killMessage,
        Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots) {
    public BotSnapshotResponse {
        botMode = botMode == null ? BotMode.SINGLE : botMode;
        difficulty = difficulty == null ? DifficultyTier.EASY : difficulty;
        minDifficulty = minDifficulty == null ? DifficultyTier.EASY : minDifficulty;
        maxDifficulty = maxDifficulty == null ? DifficultyTier.GOD : maxDifficulty;
        combatMode = combatMode == null ? CombatMode.SWORD : combatMode;
        combatTuning = combatTuning == null ? CombatTuning.builder().build() : combatTuning;
        targetUUIDs = targetUUIDs == null ? Set.of() : Set.copyOf(targetUUIDs);
        teamOwnerUUIDs = teamOwnerUUIDs == null ? Set.of() : Set.copyOf(teamOwnerUUIDs);
        source = source == null ? BotSource.CORE : source;
        targetMode = targetMode == null ? BotTargetMode.PLAYERS : targetMode;
        botNameTemplate = TextValues.orElseIfBlank(botNameTemplate, "UltimateBot");
        botSkinSource = TextValues.orElseIfBlank(botSkinSource, "RANDOM");
        minArmor = minArmor == null ? BotArmorTier.LEATHER : minArmor;
        maxArmor = maxArmor == null ? BotArmorTier.NETHERITE : maxArmor;
        armor = armor == null ? minArmor : armor;
        blastProtection = blastProtection == null ? BlastProtectionSettings.all(false) : blastProtection;
        equipmentSlots = equipmentSlots == null ? Map.of() : Map.copyOf(equipmentSlots);
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
        return source == BotSource.API;
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
        return difficulty != minDifficulty || difficulty != maxDifficulty;
    }
}
