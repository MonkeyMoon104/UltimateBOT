package com.monkey.ultimatebot.api.model.runtime;

import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlot;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotSetting;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Immutable runtime snapshot of a managed bot.
 *
 * @param ownerUUID owner UUID associated with this bot slot
 * @param botUUID runtime bot entity UUID, may be null when unavailable
 * @param botMode lifecycle and ownership mode
 * @param difficulty current difficulty
 * @param minDifficulty minimum allowed difficulty
 * @param maxDifficulty maximum allowed difficulty
 * @param combatMode selected combat mode
 * @param combatTuning resolved combat tuning
 * @param customizedCombatTuning whether the resolved tuning is a player or API override
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
public record BotSnapshot(
        UUID ownerUUID,
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
        @Nullable BotLocation spawnLocation,
        @Nullable String killMessage,
        Map<BotEquipmentSlot, BotEquipmentSlotSetting> equipmentSlots) {
    public BotSnapshot {
        Objects.requireNonNull(botMode, "botMode");
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(minDifficulty, "minDifficulty");
        Objects.requireNonNull(maxDifficulty, "maxDifficulty");
        Objects.requireNonNull(combatMode, "combatMode");
        Objects.requireNonNull(combatTuning, "combatTuning");
        targetUUIDs = targetUUIDs == null ? Set.of() : Set.copyOf(targetUUIDs);
        teamOwnerUUIDs = teamOwnerUUIDs == null ? Set.of() : Set.copyOf(teamOwnerUUIDs);
        source = source == null ? BotSource.CORE : source;
        targetMode = targetMode == null ? BotTargetMode.PLAYERS : targetMode;
        botNameTemplate = TextValues.orElseIfBlank(botNameTemplate, "UltimateBot");
        botSkinSource = TextValues.orElseIfBlank(botSkinSource, "RANDOM");
        Objects.requireNonNull(minArmor, "minArmor");
        Objects.requireNonNull(maxArmor, "maxArmor");
        Objects.requireNonNull(armor, "armor");
        Objects.requireNonNull(blastProtection, "blastProtection");
        equipmentSlots = equipmentSlots == null ? Map.of() : Map.copyOf(equipmentSlots);
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
