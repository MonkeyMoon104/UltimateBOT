package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.BlastProtectionSettings;
import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.BotMode;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import com.monkey.ultimatebot.sdk.model.type.SdkBotEquipmentSlot;
import com.monkey.ultimatebot.sdk.model.type.SdkBotEquipmentSlotMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Complete request body used to spawn any supported remote bot type.
 *
 * @param mode lifecycle and ownership mode
 * @param ownerUUID optional owner UUID for owner-bound use cases
 * @param teamOwnerUUIDs owner UUIDs used by team-allied bots
 * @param botUUID optional explicit bot UUID; {@code null} generates one automatically
 * @param targetUUIDs explicit target players for independent/event bots
 * @param botNameTemplate bot display name or name template
 * @param botSkin skin mode or player/texture reference
 * @param follow whether follow logic is active
 * @param changeableFollow whether players may change follow from the GUI
 * @param combat whether combat logic is active
 * @param changeableCombat whether players may change combat from the GUI
 * @param changeableBlast whether players may change blast protection from the GUI
 * @param blastProtection per-piece blast-protection state
 * @param changeableArmor whether players may change armor from the GUI
 * @param changeableTotem whether players may change totems from the GUI
 * @param changeableDifficulty whether players may change difficulty from the GUI
 * @param changeableCombatMode whether players may change combat mode from the GUI
 * @param armor selected armor tier
 * @param minArmor minimum random armor tier
 * @param maxArmor maximum random armor tier
 * @param totemCount selected totem count, or negative for random range
 * @param minTotemCount minimum random totem count
 * @param maxTotemCount maximum random totem count
 * @param difficulty selected difficulty profile
 * @param minDifficulty minimum random difficulty profile
 * @param maxDifficulty maximum random difficulty profile
 * @param spawnLocation optional explicit spawn position
 * @param autoTarget whether automatic target acquisition is enabled
 * @param autoTargetRange target acquisition range
 * @param attackBots whether this bot may target other managed bots
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
 * @param killMessage custom kill message, or null for default
 * @param equipmentSlots equipment-slot overrides kept active while the bot is running
 * @param combatMode selected combat mode
 * @param combatTuning optional tuning override for the selected mode and difficulty
 */
public record BotSpawnRequest(
        BotMode mode,
        @Nullable UUID ownerUUID,
        List<UUID> teamOwnerUUIDs,
        @Nullable UUID botUUID,
        List<UUID> targetUUIDs,
        String botNameTemplate,
        String botSkin,
        boolean follow,
        boolean changeableFollow,
        boolean combat,
        boolean changeableCombat,
        boolean changeableBlast,
        BlastProtectionSettings blastProtection,
        boolean changeableArmor,
        boolean changeableTotem,
        boolean changeableDifficulty,
        boolean changeableCombatMode,
        BotArmorTier armor,
        BotArmorTier minArmor,
        BotArmorTier maxArmor,
        int totemCount,
        int minTotemCount,
        int maxTotemCount,
        DifficultyTier difficulty,
        DifficultyTier minDifficulty,
        DifficultyTier maxDifficulty,
        @Nullable BotLocationRequest spawnLocation,
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
        @Nullable String killMessage,
        Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots,
        CombatMode combatMode,
        @Nullable CombatTuning combatTuning) {
    public BotSpawnRequest {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(blastProtection, "blastProtection");
        teamOwnerUUIDs = teamOwnerUUIDs == null ? List.of() : List.copyOf(teamOwnerUUIDs);
        targetUUIDs = targetUUIDs == null ? List.of() : List.copyOf(targetUUIDs);
        equipmentSlots = equipmentSlots == null ? Map.of() : Map.copyOf(equipmentSlots);
        Objects.requireNonNull(botNameTemplate, "botNameTemplate");
        Objects.requireNonNull(botSkin, "botSkin");
        Objects.requireNonNull(armor, "armor");
        Objects.requireNonNull(minArmor, "minArmor");
        Objects.requireNonNull(maxArmor, "maxArmor");
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(minDifficulty, "minDifficulty");
        Objects.requireNonNull(maxDifficulty, "maxDifficulty");
        Objects.requireNonNull(targetMode, "targetMode");
        Objects.requireNonNull(combatMode, "combatMode");
        if (botUUID != null && botUUID.equals(new UUID(0L, 0L))) {
            throw new IllegalArgumentException("botUUID cannot be the nil UUID");
        }
        if ((mode == BotMode.SINGLE || mode == BotMode.ALLY) && ownerUUID == null) {
            throw new IllegalArgumentException("ownerUUID is required for single and ally bot modes");
        }
        if (mode == BotMode.TEAM_ALLY && ownerUUID == null && teamOwnerUUIDs.isEmpty()) {
            throw new IllegalArgumentException("TEAM_ALLY requires at least one owner");
        }
        if (mode != BotMode.TEAM_ALLY && !teamOwnerUUIDs.isEmpty()) {
            throw new IllegalArgumentException("teamOwnerUUIDs are valid only for TEAM_ALLY mode");
        }
        if (botNameTemplate.isBlank()) {
            throw new IllegalArgumentException("botNameTemplate cannot be blank");
        }
        if (minArmor.compareTo(maxArmor) > 0 || armor.compareTo(minArmor) < 0 || armor.compareTo(maxArmor) > 0) {
            throw new IllegalArgumentException("armor and armor range are inconsistent");
        }
        if (minTotemCount < -1 || maxTotemCount < 0 || minTotemCount > maxTotemCount) {
            throw new IllegalArgumentException("totem range is invalid");
        }
        if ((totemCount < minTotemCount || totemCount > maxTotemCount) && (totemCount != -1 || minTotemCount > -1)) {
            throw new IllegalArgumentException("totemCount is outside its configured range");
        }
        if (minDifficulty.compareTo(maxDifficulty) > 0
                || difficulty.compareTo(minDifficulty) < 0
                || difficulty.compareTo(maxDifficulty) > 0) {
            throw new IllegalArgumentException("difficulty and difficulty range are inconsistent");
        }
        if (!follow && !changeableFollow && changeableCombat) {
            throw new IllegalArgumentException("combat cannot be changeable when follow is fixed to false");
        }
        if (combat && !changeableCombat && changeableFollow) {
            throw new IllegalArgumentException("follow cannot be changeable while combat is fixed to true");
        }
        if (!Double.isFinite(autoTargetRange) || autoTargetRange <= 0.0D) {
            throw new IllegalArgumentException("autoTargetRange must be finite and greater than 0");
        }
        if (!Double.isFinite(idleWanderRadius) || idleWanderRadius <= 0.0D) {
            throw new IllegalArgumentException("idleWanderRadius must be finite and greater than 0");
        }
        if (!Double.isFinite(idleReturnDistance) || idleReturnDistance <= 0.0D) {
            throw new IllegalArgumentException("idleReturnDistance must be finite and greater than 0");
        }
        if (idleReturnDelayMs < 0L) {
            throw new IllegalArgumentException("idleReturnDelayMs cannot be negative");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder for an independent event bot without an owner UUID.
     *
     * @return request builder
     */
    public static Builder independent() {
        return builder().mode(BotMode.EVENT).ownerUUID(null);
    }

    /**
     * Creates a builder for a bot bound to one owner UUID.
     *
     * @param ownerUUID owner UUID
     * @return request builder
     */
    public static Builder ownedBy(UUID ownerUUID) {
        return builder().mode(BotMode.SINGLE).ownerUUID(ownerUUID);
    }

    public static final class Builder {
        private BotMode mode = BotMode.EVENT;
        private @Nullable UUID ownerUUID;
        private List<UUID> teamOwnerUUIDs = List.of();
        private @Nullable UUID botUUID;
        private List<UUID> targetUUIDs = List.of();
        private String botNameTemplate = "UltimateBot";
        private String botSkin = "RANDOM";
        private boolean follow = true;
        private boolean changeableFollow = true;
        private boolean combat = true;
        private boolean changeableCombat = true;
        private boolean changeableBlast = true;
        private BlastProtectionSettings blastProtection = BlastProtectionSettings.all(false);
        private boolean changeableArmor = true;
        private boolean changeableTotem = true;
        private boolean changeableDifficulty = true;
        private boolean changeableCombatMode = true;
        private BotArmorTier armor = BotArmorTier.NETHERITE;
        private BotArmorTier minArmor = BotArmorTier.LEATHER;
        private BotArmorTier maxArmor = BotArmorTier.NETHERITE;
        private int totemCount = -1;
        private int minTotemCount = -1;
        private int maxTotemCount = 74;
        private DifficultyTier difficulty = DifficultyTier.EASY;
        private DifficultyTier minDifficulty = DifficultyTier.EASY;
        private DifficultyTier maxDifficulty = DifficultyTier.GOD;
        private @Nullable BotLocationRequest spawnLocation;
        private boolean autoTarget = true;
        private double autoTargetRange = 16.0D;
        private boolean attackBots = false;
        private BotTargetMode targetMode = BotTargetMode.PLAYERS;
        private boolean respectWorldGuardPvp = false;
        private boolean stayAfterOwnerDeath = false;
        private boolean idleWander = false;
        private double idleWanderRadius = 10.0D;
        private double idleReturnDistance = 24.0D;
        private long idleReturnDelayMs = 8000L;
        private boolean crystalPvp = true;
        private boolean explosions = true;
        private boolean explosionBlockDamage = false;
        private boolean enderPearls = true;
        private boolean healing = true;
        private boolean killMessageEnabled = true;
        private @Nullable String killMessage;
        private Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots = Map.of();
        private CombatMode combatMode = CombatMode.SWORD;
        private @Nullable CombatTuning combatTuning;

        private Builder() {}

        public Builder mode(BotMode mode) {
            this.mode = Objects.requireNonNull(mode, "mode");
            return this;
        }

        public Builder ownerUUID(@Nullable UUID ownerUUID) {
            this.ownerUUID = ownerUUID;
            return this;
        }

        public Builder teamOwnerUUIDs(@Nullable List<UUID> teamOwnerUUIDs) {
            this.teamOwnerUUIDs = teamOwnerUUIDs == null ? List.of() : List.copyOf(teamOwnerUUIDs);
            return this;
        }

        /** Sets a specific bot UUID, or {@code null} to generate one automatically. */
        public Builder botUUID(@Nullable UUID botUUID) {
            this.botUUID = botUUID;
            return this;
        }

        public Builder targetUUIDs(@Nullable List<UUID> targetUUIDs) {
            this.targetUUIDs = targetUUIDs == null ? List.of() : List.copyOf(targetUUIDs);
            return this;
        }

        public Builder botNameTemplate(String botNameTemplate) {
            this.botNameTemplate = Objects.requireNonNull(botNameTemplate, "botNameTemplate");
            return this;
        }

        public Builder botSkin(String botSkin) {
            this.botSkin = Objects.requireNonNull(botSkin, "botSkin");
            return this;
        }

        public Builder follow(boolean follow) {
            this.follow = follow;
            return this;
        }

        public Builder changeableFollow(boolean changeableFollow) {
            this.changeableFollow = changeableFollow;
            return this;
        }

        public Builder combat(boolean combat) {
            this.combat = combat;
            return this;
        }

        public Builder changeableCombat(boolean changeableCombat) {
            this.changeableCombat = changeableCombat;
            return this;
        }

        public Builder changeableBlast(boolean changeableBlast) {
            this.changeableBlast = changeableBlast;
            return this;
        }

        public Builder blastProtection(BlastProtectionSettings blastProtection) {
            this.blastProtection = Objects.requireNonNull(blastProtection, "blastProtection");
            return this;
        }

        public Builder blastProtection(boolean enabled) {
            return blastProtection(BlastProtectionSettings.all(enabled));
        }

        public Builder changeableArmor(boolean changeableArmor) {
            this.changeableArmor = changeableArmor;
            return this;
        }

        public Builder changeableTotem(boolean changeableTotem) {
            this.changeableTotem = changeableTotem;
            return this;
        }

        public Builder changeableDifficulty(boolean changeableDifficulty) {
            this.changeableDifficulty = changeableDifficulty;
            return this;
        }

        public Builder changeableCombatMode(boolean changeableCombatMode) {
            this.changeableCombatMode = changeableCombatMode;
            return this;
        }

        public Builder armor(BotArmorTier armor) {
            this.armor = Objects.requireNonNull(armor, "armor");
            return this;
        }

        public Builder armorRange(BotArmorTier minArmor, BotArmorTier maxArmor) {
            this.minArmor = Objects.requireNonNull(minArmor, "minArmor");
            this.maxArmor = Objects.requireNonNull(maxArmor, "maxArmor");
            return this;
        }

        public Builder totemCount(int totemCount) {
            this.totemCount = totemCount;
            return this;
        }

        public Builder totemRange(int minTotemCount, int maxTotemCount) {
            this.minTotemCount = minTotemCount;
            this.maxTotemCount = maxTotemCount;
            return this;
        }

        public Builder difficulty(DifficultyTier difficulty) {
            this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
            return this;
        }

        public Builder difficultyRange(DifficultyTier minDifficulty, DifficultyTier maxDifficulty) {
            this.minDifficulty = Objects.requireNonNull(minDifficulty, "minDifficulty");
            this.maxDifficulty = Objects.requireNonNull(maxDifficulty, "maxDifficulty");
            return this;
        }

        public Builder spawnLocation(@Nullable BotLocationRequest spawnLocation) {
            this.spawnLocation = spawnLocation;
            return this;
        }

        public Builder autoTarget(boolean autoTarget) {
            this.autoTarget = autoTarget;
            return this;
        }

        public Builder autoTargetRange(double autoTargetRange) {
            this.autoTargetRange = autoTargetRange;
            return this;
        }

        public Builder attackBots(boolean attackBots) {
            this.attackBots = attackBots;
            return this;
        }

        public Builder targetMode(BotTargetMode targetMode) {
            this.targetMode = Objects.requireNonNull(targetMode, "targetMode");
            return this;
        }

        public Builder combatMode(CombatMode combatMode) {
            this.combatMode = Objects.requireNonNull(combatMode, "combatMode");
            return this;
        }

        public Builder combatTuning(@Nullable CombatTuning combatTuning) {
            this.combatTuning = combatTuning;
            return this;
        }

        public Builder respectWorldGuardPvp(boolean respectWorldGuardPvp) {
            this.respectWorldGuardPvp = respectWorldGuardPvp;
            return this;
        }

        public Builder stayAfterOwnerDeath(boolean stayAfterOwnerDeath) {
            this.stayAfterOwnerDeath = stayAfterOwnerDeath;
            return this;
        }

        public Builder idleWander(boolean idleWander) {
            this.idleWander = idleWander;
            return this;
        }

        public Builder idleWanderRadius(double idleWanderRadius) {
            this.idleWanderRadius = idleWanderRadius;
            return this;
        }

        public Builder idleReturnDistance(double idleReturnDistance) {
            this.idleReturnDistance = idleReturnDistance;
            return this;
        }

        public Builder idleReturnDelayMs(long idleReturnDelayMs) {
            this.idleReturnDelayMs = idleReturnDelayMs;
            return this;
        }

        public Builder crystalPvp(boolean crystalPvp) {
            this.crystalPvp = crystalPvp;
            return this;
        }

        public Builder explosions(boolean explosions) {
            this.explosions = explosions;
            return this;
        }

        public Builder explosionBlockDamage(boolean explosionBlockDamage) {
            this.explosionBlockDamage = explosionBlockDamage;
            return this;
        }

        public Builder enderPearls(boolean enderPearls) {
            this.enderPearls = enderPearls;
            return this;
        }

        public Builder healing(boolean healing) {
            this.healing = healing;
            return this;
        }

        public Builder disableHealing() {
            this.healing = false;
            return this;
        }

        public Builder killMessage(String killMessage) {
            this.killMessageEnabled = true;
            this.killMessage = Objects.requireNonNull(killMessage, "killMessage");
            return this;
        }

        public Builder disableKillMessage() {
            this.killMessageEnabled = false;
            this.killMessage = null;
            return this;
        }

        public Builder equipmentSlots(@Nullable Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots) {
            this.equipmentSlots = equipmentSlots == null ? Map.of() : Map.copyOf(equipmentSlots);
            return this;
        }

        public Builder equipmentSlot(SdkBotEquipmentSlot slot, BotEquipmentSlotRequest setting) {
            java.util.EnumMap<SdkBotEquipmentSlot, BotEquipmentSlotRequest> updated =
                    new java.util.EnumMap<>(SdkBotEquipmentSlot.class);
            updated.putAll(equipmentSlots);
            BotEquipmentSlotRequest requiredSetting = Objects.requireNonNull(setting, "setting");
            if (requiredSetting.mode() == SdkBotEquipmentSlotMode.DEFAULT) {
                updated.remove(Objects.requireNonNull(slot, "slot"));
            } else {
                updated.put(Objects.requireNonNull(slot, "slot"), requiredSetting);
            }
            this.equipmentSlots = Map.copyOf(updated);
            return this;
        }

        public Builder emptyEquipmentSlot(SdkBotEquipmentSlot slot) {
            return equipmentSlot(slot, BotEquipmentSlotRequest.empty());
        }

        public Builder equipmentItem(SdkBotEquipmentSlot slot, String material, int amount) {
            return equipmentSlot(slot, BotEquipmentSlotRequest.item(material, amount));
        }

        public Builder defaultEquipmentSlot(SdkBotEquipmentSlot slot) {
            return equipmentSlot(slot, BotEquipmentSlotRequest.defaultSlot());
        }

        public Builder disableExplosiveCombat() {
            this.crystalPvp = false;
            this.explosions = false;
            return this;
        }

        public Builder stationary() {
            this.idleWander = false;
            return this;
        }

        public Builder wander(double radius, double returnDistance, long returnDelayMs) {
            this.idleWander = true;
            this.idleWanderRadius = radius;
            this.idleReturnDistance = returnDistance;
            this.idleReturnDelayMs = returnDelayMs;
            return this;
        }

        public BotSpawnRequest build() {
            return new BotSpawnRequest(
                    mode,
                    ownerUUID,
                    teamOwnerUUIDs,
                    botUUID,
                    targetUUIDs,
                    botNameTemplate,
                    botSkin,
                    follow,
                    changeableFollow,
                    combat,
                    changeableCombat,
                    changeableBlast,
                    blastProtection,
                    changeableArmor,
                    changeableTotem,
                    changeableDifficulty,
                    changeableCombatMode,
                    armor,
                    minArmor,
                    maxArmor,
                    totemCount,
                    minTotemCount,
                    maxTotemCount,
                    difficulty,
                    minDifficulty,
                    maxDifficulty,
                    spawnLocation,
                    autoTarget,
                    autoTargetRange,
                    attackBots,
                    targetMode,
                    respectWorldGuardPvp,
                    stayAfterOwnerDeath,
                    idleWander,
                    idleWanderRadius,
                    idleReturnDistance,
                    idleReturnDelayMs,
                    crystalPvp,
                    explosions,
                    explosionBlockDamage,
                    enderPearls,
                    healing,
                    killMessageEnabled,
                    killMessage,
                    equipmentSlots,
                    combatMode,
                    combatTuning);
        }
    }
}
