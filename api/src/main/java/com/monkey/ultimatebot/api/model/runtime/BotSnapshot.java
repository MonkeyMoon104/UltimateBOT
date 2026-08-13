package com.monkey.ultimatebot.api.model.runtime;


import java.util.Collections;
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
public final class BotSnapshot {
    private final UUID ownerUUID;
    private final @Nullable UUID botUUID;
    private final BotMode botMode;
    private final DifficultyTier difficulty;
    private final DifficultyTier minDifficulty;
    private final DifficultyTier maxDifficulty;
    private final CombatMode combatMode;
    private final @Nullable BrainKey brain;
    private final CombatTuning combatTuning;
    private final boolean customizedCombatTuning;
    private final boolean follow;
    private final boolean combat;
    private final int totemCount;
    private final int minTotemCount;
    private final int maxTotemCount;
    private final @Nullable UUID targetUUID;
    private final Set<UUID> targetUUIDs;
    private final BotSource source;
    private final boolean autoTarget;
    private final double autoTargetRange;
    private final boolean attackBots;
    private final BotTargetMode targetMode;
    private final boolean respectWorldGuardPvp;
    private final boolean stayAfterOwnerDeath;
    private final boolean idleWander;
    private final double idleWanderRadius;
    private final double idleReturnDistance;
    private final long idleReturnDelayMs;
    private final boolean crystalPvp;
    private final boolean explosions;
    private final boolean explosionBlockDamage;
    private final boolean enderPearls;
    private final boolean healing;
    private final boolean killMessageEnabled;
    private final Set<UUID> teamOwnerUUIDs;
    private final String botNameTemplate;
    private final String botSkinSource;
    private final BotArmorTier minArmor;
    private final BotArmorTier maxArmor;
    private final BotArmorTier armor;
    private final BlastProtectionSettings blastProtection;
    private final boolean changeableFollow;
    private final boolean changeableCombat;
    private final boolean changeableBlast;
    private final boolean changeableArmor;
    private final boolean changeableTotem;
    private final boolean changeableDifficulty;
    private final boolean changeableCombatMode;
    private final @Nullable BotLocation spawnLocation;
    private final @Nullable String killMessage;
    private final Map<BotEquipmentSlot, BotEquipmentSlotSetting> equipmentSlots;

    public BotSnapshot(UUID ownerUUID, @Nullable UUID botUUID, BotMode botMode, DifficultyTier difficulty, DifficultyTier minDifficulty, DifficultyTier maxDifficulty, CombatMode combatMode, @Nullable BrainKey brain, CombatTuning combatTuning, boolean customizedCombatTuning, boolean follow, boolean combat, int totemCount, int minTotemCount, int maxTotemCount, @Nullable UUID targetUUID, Set<UUID> targetUUIDs, BotSource source, boolean autoTarget, double autoTargetRange, boolean attackBots, BotTargetMode targetMode, boolean respectWorldGuardPvp, boolean stayAfterOwnerDeath, boolean idleWander, double idleWanderRadius, double idleReturnDistance, long idleReturnDelayMs, boolean crystalPvp, boolean explosions, boolean explosionBlockDamage, boolean enderPearls, boolean healing, boolean killMessageEnabled, Set<UUID> teamOwnerUUIDs, String botNameTemplate, String botSkinSource, BotArmorTier minArmor, BotArmorTier maxArmor, BotArmorTier armor, BlastProtectionSettings blastProtection, boolean changeableFollow, boolean changeableCombat, boolean changeableBlast, boolean changeableArmor, boolean changeableTotem, boolean changeableDifficulty, boolean changeableCombatMode, @Nullable BotLocation spawnLocation, @Nullable String killMessage, Map<BotEquipmentSlot, BotEquipmentSlotSetting> equipmentSlots) {


        Objects.requireNonNull(botMode, "botMode");
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(minDifficulty, "minDifficulty");
        Objects.requireNonNull(maxDifficulty, "maxDifficulty");
        Objects.requireNonNull(combatMode, "combatMode");
        Objects.requireNonNull(combatTuning, "combatTuning");
        targetUUIDs = targetUUIDs == null ? Collections.emptySet() : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(targetUUIDs);
        teamOwnerUUIDs = teamOwnerUUIDs == null ? Collections.emptySet() : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(teamOwnerUUIDs);
        source = source == null ? BotSource.CORE : source;
        targetMode = targetMode == null ? BotTargetMode.PLAYERS : targetMode;
        botNameTemplate = TextValues.orElseIfBlank(botNameTemplate, "UltimateBot");
        botSkinSource = TextValues.orElseIfBlank(botSkinSource, "RANDOM");
        Objects.requireNonNull(minArmor, "minArmor");
        Objects.requireNonNull(maxArmor, "maxArmor");
        Objects.requireNonNull(armor, "armor");
        Objects.requireNonNull(blastProtection, "blastProtection");
        equipmentSlots = equipmentSlots == null ? Collections.emptyMap() : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(equipmentSlots);
        this.ownerUUID = ownerUUID;
        this.botUUID = botUUID;
        this.botMode = botMode;
        this.difficulty = difficulty;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
        this.combatMode = combatMode;
        this.brain = brain;
        this.combatTuning = combatTuning;
        this.customizedCombatTuning = customizedCombatTuning;
        this.follow = follow;
        this.combat = combat;
        this.totemCount = totemCount;
        this.minTotemCount = minTotemCount;
        this.maxTotemCount = maxTotemCount;
        this.targetUUID = targetUUID;
        this.targetUUIDs = targetUUIDs;
        this.source = source;
        this.autoTarget = autoTarget;
        this.autoTargetRange = autoTargetRange;
        this.attackBots = attackBots;
        this.targetMode = targetMode;
        this.respectWorldGuardPvp = respectWorldGuardPvp;
        this.stayAfterOwnerDeath = stayAfterOwnerDeath;
        this.idleWander = idleWander;
        this.idleWanderRadius = idleWanderRadius;
        this.idleReturnDistance = idleReturnDistance;
        this.idleReturnDelayMs = idleReturnDelayMs;
        this.crystalPvp = crystalPvp;
        this.explosions = explosions;
        this.explosionBlockDamage = explosionBlockDamage;
        this.enderPearls = enderPearls;
        this.healing = healing;
        this.killMessageEnabled = killMessageEnabled;
        this.teamOwnerUUIDs = teamOwnerUUIDs;
        this.botNameTemplate = botNameTemplate;
        this.botSkinSource = botSkinSource;
        this.minArmor = minArmor;
        this.maxArmor = maxArmor;
        this.armor = armor;
        this.blastProtection = blastProtection;
        this.changeableFollow = changeableFollow;
        this.changeableCombat = changeableCombat;
        this.changeableBlast = changeableBlast;
        this.changeableArmor = changeableArmor;
        this.changeableTotem = changeableTotem;
        this.changeableDifficulty = changeableDifficulty;
        this.changeableCombatMode = changeableCombatMode;
        this.spawnLocation = spawnLocation;
        this.killMessage = killMessage;
        this.equipmentSlots = equipmentSlots;
    }

    public UUID ownerUUID() {
        return ownerUUID;
    }
    public @Nullable UUID botUUID() {
        return botUUID;
    }
    public BotMode botMode() {
        return botMode;
    }
    public DifficultyTier difficulty() {
        return difficulty;
    }
    public DifficultyTier minDifficulty() {
        return minDifficulty;
    }
    public DifficultyTier maxDifficulty() {
        return maxDifficulty;
    }
    public CombatMode combatMode() {
        return combatMode;
    }
    public @Nullable BrainKey brain() {
        return brain;
    }
    public CombatTuning combatTuning() {
        return combatTuning;
    }
    public boolean customizedCombatTuning() {
        return customizedCombatTuning;
    }
    public boolean follow() {
        return follow;
    }
    public boolean combat() {
        return combat;
    }
    public int totemCount() {
        return totemCount;
    }
    public int minTotemCount() {
        return minTotemCount;
    }
    public int maxTotemCount() {
        return maxTotemCount;
    }
    public @Nullable UUID targetUUID() {
        return targetUUID;
    }
    public Set<UUID> targetUUIDs() {
        return targetUUIDs;
    }
    public BotSource source() {
        return source;
    }
    public boolean autoTarget() {
        return autoTarget;
    }
    public double autoTargetRange() {
        return autoTargetRange;
    }
    public boolean attackBots() {
        return attackBots;
    }
    public BotTargetMode targetMode() {
        return targetMode;
    }
    public boolean respectWorldGuardPvp() {
        return respectWorldGuardPvp;
    }
    public boolean stayAfterOwnerDeath() {
        return stayAfterOwnerDeath;
    }
    public boolean idleWander() {
        return idleWander;
    }
    public double idleWanderRadius() {
        return idleWanderRadius;
    }
    public double idleReturnDistance() {
        return idleReturnDistance;
    }
    public long idleReturnDelayMs() {
        return idleReturnDelayMs;
    }
    public boolean crystalPvp() {
        return crystalPvp;
    }
    public boolean explosions() {
        return explosions;
    }
    public boolean explosionBlockDamage() {
        return explosionBlockDamage;
    }
    public boolean enderPearls() {
        return enderPearls;
    }
    public boolean healing() {
        return healing;
    }
    public boolean killMessageEnabled() {
        return killMessageEnabled;
    }
    public Set<UUID> teamOwnerUUIDs() {
        return teamOwnerUUIDs;
    }
    public String botNameTemplate() {
        return botNameTemplate;
    }
    public String botSkinSource() {
        return botSkinSource;
    }
    public BotArmorTier minArmor() {
        return minArmor;
    }
    public BotArmorTier maxArmor() {
        return maxArmor;
    }
    public BotArmorTier armor() {
        return armor;
    }
    public BlastProtectionSettings blastProtection() {
        return blastProtection;
    }
    public boolean changeableFollow() {
        return changeableFollow;
    }
    public boolean changeableCombat() {
        return changeableCombat;
    }
    public boolean changeableBlast() {
        return changeableBlast;
    }
    public boolean changeableArmor() {
        return changeableArmor;
    }
    public boolean changeableTotem() {
        return changeableTotem;
    }
    public boolean changeableDifficulty() {
        return changeableDifficulty;
    }
    public boolean changeableCombatMode() {
        return changeableCombatMode;
    }
    public @Nullable BotLocation spawnLocation() {
        return spawnLocation;
    }
    public @Nullable String killMessage() {
        return killMessage;
    }
    public Map<BotEquipmentSlot, BotEquipmentSlotSetting> equipmentSlots() {
        return equipmentSlots;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotSnapshot)) {
            return false;
        }
        BotSnapshot other = (BotSnapshot) obj;
        return java.util.Objects.equals(ownerUUID, other.ownerUUID) && java.util.Objects.equals(botUUID, other.botUUID) && java.util.Objects.equals(botMode, other.botMode) && java.util.Objects.equals(difficulty, other.difficulty) && java.util.Objects.equals(minDifficulty, other.minDifficulty) && java.util.Objects.equals(maxDifficulty, other.maxDifficulty) && java.util.Objects.equals(combatMode, other.combatMode) && java.util.Objects.equals(brain, other.brain) && java.util.Objects.equals(combatTuning, other.combatTuning) && customizedCombatTuning == other.customizedCombatTuning && follow == other.follow && combat == other.combat && totemCount == other.totemCount && minTotemCount == other.minTotemCount && maxTotemCount == other.maxTotemCount && java.util.Objects.equals(targetUUID, other.targetUUID) && java.util.Objects.equals(targetUUIDs, other.targetUUIDs) && java.util.Objects.equals(source, other.source) && autoTarget == other.autoTarget && Double.compare(autoTargetRange, other.autoTargetRange) == 0 && attackBots == other.attackBots && java.util.Objects.equals(targetMode, other.targetMode) && respectWorldGuardPvp == other.respectWorldGuardPvp && stayAfterOwnerDeath == other.stayAfterOwnerDeath && idleWander == other.idleWander && Double.compare(idleWanderRadius, other.idleWanderRadius) == 0 && Double.compare(idleReturnDistance, other.idleReturnDistance) == 0 && idleReturnDelayMs == other.idleReturnDelayMs && crystalPvp == other.crystalPvp && explosions == other.explosions && explosionBlockDamage == other.explosionBlockDamage && enderPearls == other.enderPearls && healing == other.healing && killMessageEnabled == other.killMessageEnabled && java.util.Objects.equals(teamOwnerUUIDs, other.teamOwnerUUIDs) && java.util.Objects.equals(botNameTemplate, other.botNameTemplate) && java.util.Objects.equals(botSkinSource, other.botSkinSource) && java.util.Objects.equals(minArmor, other.minArmor) && java.util.Objects.equals(maxArmor, other.maxArmor) && java.util.Objects.equals(armor, other.armor) && java.util.Objects.equals(blastProtection, other.blastProtection) && changeableFollow == other.changeableFollow && changeableCombat == other.changeableCombat && changeableBlast == other.changeableBlast && changeableArmor == other.changeableArmor && changeableTotem == other.changeableTotem && changeableDifficulty == other.changeableDifficulty && changeableCombatMode == other.changeableCombatMode && java.util.Objects.equals(spawnLocation, other.spawnLocation) && java.util.Objects.equals(killMessage, other.killMessage) && java.util.Objects.equals(equipmentSlots, other.equipmentSlots);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(ownerUUID, botUUID, botMode, difficulty, minDifficulty, maxDifficulty, combatMode, brain, combatTuning, customizedCombatTuning, follow, combat, totemCount, minTotemCount, maxTotemCount, targetUUID, targetUUIDs, source, autoTarget, autoTargetRange, attackBots, targetMode, respectWorldGuardPvp, stayAfterOwnerDeath, idleWander, idleWanderRadius, idleReturnDistance, idleReturnDelayMs, crystalPvp, explosions, explosionBlockDamage, enderPearls, healing, killMessageEnabled, teamOwnerUUIDs, botNameTemplate, botSkinSource, minArmor, maxArmor, armor, blastProtection, changeableFollow, changeableCombat, changeableBlast, changeableArmor, changeableTotem, changeableDifficulty, changeableCombatMode, spawnLocation, killMessage, equipmentSlots);
    }

    @Override
    public String toString() {
        return "BotSnapshot[ownerUUID=" + ownerUUID + ", botUUID=" + botUUID + ", botMode=" + botMode + ", difficulty=" + difficulty + ", minDifficulty=" + minDifficulty + ", maxDifficulty=" + maxDifficulty + ", combatMode=" + combatMode + ", brain=" + brain + ", combatTuning=" + combatTuning + ", customizedCombatTuning=" + customizedCombatTuning + ", follow=" + follow + ", combat=" + combat + ", totemCount=" + totemCount + ", minTotemCount=" + minTotemCount + ", maxTotemCount=" + maxTotemCount + ", targetUUID=" + targetUUID + ", targetUUIDs=" + targetUUIDs + ", source=" + source + ", autoTarget=" + autoTarget + ", autoTargetRange=" + autoTargetRange + ", attackBots=" + attackBots + ", targetMode=" + targetMode + ", respectWorldGuardPvp=" + respectWorldGuardPvp + ", stayAfterOwnerDeath=" + stayAfterOwnerDeath + ", idleWander=" + idleWander + ", idleWanderRadius=" + idleWanderRadius + ", idleReturnDistance=" + idleReturnDistance + ", idleReturnDelayMs=" + idleReturnDelayMs + ", crystalPvp=" + crystalPvp + ", explosions=" + explosions + ", explosionBlockDamage=" + explosionBlockDamage + ", enderPearls=" + enderPearls + ", healing=" + healing + ", killMessageEnabled=" + killMessageEnabled + ", teamOwnerUUIDs=" + teamOwnerUUIDs + ", botNameTemplate=" + botNameTemplate + ", botSkinSource=" + botSkinSource + ", minArmor=" + minArmor + ", maxArmor=" + maxArmor + ", armor=" + armor + ", blastProtection=" + blastProtection + ", changeableFollow=" + changeableFollow + ", changeableCombat=" + changeableCombat + ", changeableBlast=" + changeableBlast + ", changeableArmor=" + changeableArmor + ", changeableTotem=" + changeableTotem + ", changeableDifficulty=" + changeableDifficulty + ", changeableCombatMode=" + changeableCombatMode + ", spawnLocation=" + spawnLocation + ", killMessage=" + killMessage + ", equipmentSlots=" + equipmentSlots + "]";
    }
}
