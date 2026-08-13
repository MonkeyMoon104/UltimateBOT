package com.monkey.ultimatebot.sdk.model.request;


import java.util.Collections;
import com.monkey.ultimatebot.common.model.BlastProtectionSettings;
import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.BotMode;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.common.model.BrainKey;
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
 * @param brain optional custom brain; {@code null} uses the brain assigned to the mode
 * @param combatTuning optional tuning override for the selected mode and difficulty
 */
public final class BotSpawnRequest {
    private final BotMode mode;
    private final UUID ownerUUID;
    private final List<UUID> teamOwnerUUIDs;
    private final UUID botUUID;
    private final List<UUID> targetUUIDs;
    private final String botNameTemplate;
    private final String botSkin;
    private final boolean follow;
    private final boolean changeableFollow;
    private final boolean combat;
    private final boolean changeableCombat;
    private final boolean changeableBlast;
    private final BlastProtectionSettings blastProtection;
    private final boolean changeableArmor;
    private final boolean changeableTotem;
    private final boolean changeableDifficulty;
    private final boolean changeableCombatMode;
    private final BotArmorTier armor;
    private final BotArmorTier minArmor;
    private final BotArmorTier maxArmor;
    private final int totemCount;
    private final int minTotemCount;
    private final int maxTotemCount;
    private final DifficultyTier difficulty;
    private final DifficultyTier minDifficulty;
    private final DifficultyTier maxDifficulty;
    private final BotLocationRequest spawnLocation;
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
    private final String killMessage;
    private final Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots;
    private final CombatMode combatMode;
    private final BrainKey brain;
    private final CombatTuning combatTuning;

    public BotSpawnRequest(BotMode mode, UUID ownerUUID, List<UUID> teamOwnerUUIDs, UUID botUUID, List<UUID> targetUUIDs, String botNameTemplate, String botSkin, boolean follow, boolean changeableFollow, boolean combat, boolean changeableCombat, boolean changeableBlast, BlastProtectionSettings blastProtection, boolean changeableArmor, boolean changeableTotem, boolean changeableDifficulty, boolean changeableCombatMode, BotArmorTier armor, BotArmorTier minArmor, BotArmorTier maxArmor, int totemCount, int minTotemCount, int maxTotemCount, DifficultyTier difficulty, DifficultyTier minDifficulty, DifficultyTier maxDifficulty, BotLocationRequest spawnLocation, boolean autoTarget, double autoTargetRange, boolean attackBots, BotTargetMode targetMode, boolean respectWorldGuardPvp, boolean stayAfterOwnerDeath, boolean idleWander, double idleWanderRadius, double idleReturnDistance, long idleReturnDelayMs, boolean crystalPvp, boolean explosions, boolean explosionBlockDamage, boolean enderPearls, boolean healing, boolean killMessageEnabled, String killMessage, Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots, CombatMode combatMode, BrainKey brain, CombatTuning combatTuning) {


        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(blastProtection, "blastProtection");
        teamOwnerUUIDs = teamOwnerUUIDs == null ? Collections.emptyList() : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(teamOwnerUUIDs);
        targetUUIDs = targetUUIDs == null ? Collections.emptyList() : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(targetUUIDs);
        equipmentSlots = equipmentSlots == null ? Collections.emptyMap() : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(equipmentSlots);
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
        if (botNameTemplate.trim().isEmpty()) {
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
        this.mode = mode;
        this.ownerUUID = ownerUUID;
        this.teamOwnerUUIDs = teamOwnerUUIDs;
        this.botUUID = botUUID;
        this.targetUUIDs = targetUUIDs;
        this.botNameTemplate = botNameTemplate;
        this.botSkin = botSkin;
        this.follow = follow;
        this.changeableFollow = changeableFollow;
        this.combat = combat;
        this.changeableCombat = changeableCombat;
        this.changeableBlast = changeableBlast;
        this.blastProtection = blastProtection;
        this.changeableArmor = changeableArmor;
        this.changeableTotem = changeableTotem;
        this.changeableDifficulty = changeableDifficulty;
        this.changeableCombatMode = changeableCombatMode;
        this.armor = armor;
        this.minArmor = minArmor;
        this.maxArmor = maxArmor;
        this.totemCount = totemCount;
        this.minTotemCount = minTotemCount;
        this.maxTotemCount = maxTotemCount;
        this.difficulty = difficulty;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
        this.spawnLocation = spawnLocation;
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
        this.killMessage = killMessage;
        this.equipmentSlots = equipmentSlots;
        this.combatMode = combatMode;
        this.brain = brain;
        this.combatTuning = combatTuning;
    }

    public BotMode mode() {
        return mode;
    }
    public UUID ownerUUID() {
        return ownerUUID;
    }
    public List<UUID> teamOwnerUUIDs() {
        return teamOwnerUUIDs;
    }
    public UUID botUUID() {
        return botUUID;
    }
    public List<UUID> targetUUIDs() {
        return targetUUIDs;
    }
    public String botNameTemplate() {
        return botNameTemplate;
    }
    public String botSkin() {
        return botSkin;
    }
    public boolean follow() {
        return follow;
    }
    public boolean changeableFollow() {
        return changeableFollow;
    }
    public boolean combat() {
        return combat;
    }
    public boolean changeableCombat() {
        return changeableCombat;
    }
    public boolean changeableBlast() {
        return changeableBlast;
    }
    public BlastProtectionSettings blastProtection() {
        return blastProtection;
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
    public BotArmorTier armor() {
        return armor;
    }
    public BotArmorTier minArmor() {
        return minArmor;
    }
    public BotArmorTier maxArmor() {
        return maxArmor;
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
    public DifficultyTier difficulty() {
        return difficulty;
    }
    public DifficultyTier minDifficulty() {
        return minDifficulty;
    }
    public DifficultyTier maxDifficulty() {
        return maxDifficulty;
    }
    public BotLocationRequest spawnLocation() {
        return spawnLocation;
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
    public String killMessage() {
        return killMessage;
    }
    public Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots() {
        return equipmentSlots;
    }
    public CombatMode combatMode() {
        return combatMode;
    }
    public BrainKey brain() {
        return brain;
    }
    public CombatTuning combatTuning() {
        return combatTuning;
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
        private List<UUID> teamOwnerUUIDs = Collections.emptyList();
        private @Nullable UUID botUUID;
        private List<UUID> targetUUIDs = Collections.emptyList();
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
        private Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots = Collections.emptyMap();
        private CombatMode combatMode = CombatMode.SWORD;
        private @Nullable BrainKey brain;
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
            this.teamOwnerUUIDs = teamOwnerUUIDs == null ? Collections.emptyList() : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(teamOwnerUUIDs);
            return this;
        }

        /** Sets a specific bot UUID, or {@code null} to generate one automatically. */
        public Builder botUUID(@Nullable UUID botUUID) {
            this.botUUID = botUUID;
            return this;
        }

        public Builder targetUUIDs(@Nullable List<UUID> targetUUIDs) {
            this.targetUUIDs = targetUUIDs == null ? Collections.emptyList() : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(targetUUIDs);
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

        public Builder brain(@Nullable BrainKey brain) {
            this.brain = brain;
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
            this.equipmentSlots = equipmentSlots == null ? Collections.emptyMap() : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(equipmentSlots);
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
            this.equipmentSlots = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(updated);
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
                    brain,
                    combatTuning);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotSpawnRequest)) {
            return false;
        }
        BotSpawnRequest other = (BotSpawnRequest) obj;
        return java.util.Objects.equals(mode, other.mode) && java.util.Objects.equals(ownerUUID, other.ownerUUID) && java.util.Objects.equals(teamOwnerUUIDs, other.teamOwnerUUIDs) && java.util.Objects.equals(botUUID, other.botUUID) && java.util.Objects.equals(targetUUIDs, other.targetUUIDs) && java.util.Objects.equals(botNameTemplate, other.botNameTemplate) && java.util.Objects.equals(botSkin, other.botSkin) && follow == other.follow && changeableFollow == other.changeableFollow && combat == other.combat && changeableCombat == other.changeableCombat && changeableBlast == other.changeableBlast && java.util.Objects.equals(blastProtection, other.blastProtection) && changeableArmor == other.changeableArmor && changeableTotem == other.changeableTotem && changeableDifficulty == other.changeableDifficulty && changeableCombatMode == other.changeableCombatMode && java.util.Objects.equals(armor, other.armor) && java.util.Objects.equals(minArmor, other.minArmor) && java.util.Objects.equals(maxArmor, other.maxArmor) && totemCount == other.totemCount && minTotemCount == other.minTotemCount && maxTotemCount == other.maxTotemCount && java.util.Objects.equals(difficulty, other.difficulty) && java.util.Objects.equals(minDifficulty, other.minDifficulty) && java.util.Objects.equals(maxDifficulty, other.maxDifficulty) && java.util.Objects.equals(spawnLocation, other.spawnLocation) && autoTarget == other.autoTarget && Double.compare(autoTargetRange, other.autoTargetRange) == 0 && attackBots == other.attackBots && java.util.Objects.equals(targetMode, other.targetMode) && respectWorldGuardPvp == other.respectWorldGuardPvp && stayAfterOwnerDeath == other.stayAfterOwnerDeath && idleWander == other.idleWander && Double.compare(idleWanderRadius, other.idleWanderRadius) == 0 && Double.compare(idleReturnDistance, other.idleReturnDistance) == 0 && idleReturnDelayMs == other.idleReturnDelayMs && crystalPvp == other.crystalPvp && explosions == other.explosions && explosionBlockDamage == other.explosionBlockDamage && enderPearls == other.enderPearls && healing == other.healing && killMessageEnabled == other.killMessageEnabled && java.util.Objects.equals(killMessage, other.killMessage) && java.util.Objects.equals(equipmentSlots, other.equipmentSlots) && java.util.Objects.equals(combatMode, other.combatMode) && java.util.Objects.equals(brain, other.brain) && java.util.Objects.equals(combatTuning, other.combatTuning);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(mode, ownerUUID, teamOwnerUUIDs, botUUID, targetUUIDs, botNameTemplate, botSkin, follow, changeableFollow, combat, changeableCombat, changeableBlast, blastProtection, changeableArmor, changeableTotem, changeableDifficulty, changeableCombatMode, armor, minArmor, maxArmor, totemCount, minTotemCount, maxTotemCount, difficulty, minDifficulty, maxDifficulty, spawnLocation, autoTarget, autoTargetRange, attackBots, targetMode, respectWorldGuardPvp, stayAfterOwnerDeath, idleWander, idleWanderRadius, idleReturnDistance, idleReturnDelayMs, crystalPvp, explosions, explosionBlockDamage, enderPearls, healing, killMessageEnabled, killMessage, equipmentSlots, combatMode, brain, combatTuning);
    }

    @Override
    public String toString() {
        return "BotSpawnRequest[mode=" + mode + ", ownerUUID=" + ownerUUID + ", teamOwnerUUIDs=" + teamOwnerUUIDs + ", botUUID=" + botUUID + ", targetUUIDs=" + targetUUIDs + ", botNameTemplate=" + botNameTemplate + ", botSkin=" + botSkin + ", follow=" + follow + ", changeableFollow=" + changeableFollow + ", combat=" + combat + ", changeableCombat=" + changeableCombat + ", changeableBlast=" + changeableBlast + ", blastProtection=" + blastProtection + ", changeableArmor=" + changeableArmor + ", changeableTotem=" + changeableTotem + ", changeableDifficulty=" + changeableDifficulty + ", changeableCombatMode=" + changeableCombatMode + ", armor=" + armor + ", minArmor=" + minArmor + ", maxArmor=" + maxArmor + ", totemCount=" + totemCount + ", minTotemCount=" + minTotemCount + ", maxTotemCount=" + maxTotemCount + ", difficulty=" + difficulty + ", minDifficulty=" + minDifficulty + ", maxDifficulty=" + maxDifficulty + ", spawnLocation=" + spawnLocation + ", autoTarget=" + autoTarget + ", autoTargetRange=" + autoTargetRange + ", attackBots=" + attackBots + ", targetMode=" + targetMode + ", respectWorldGuardPvp=" + respectWorldGuardPvp + ", stayAfterOwnerDeath=" + stayAfterOwnerDeath + ", idleWander=" + idleWander + ", idleWanderRadius=" + idleWanderRadius + ", idleReturnDistance=" + idleReturnDistance + ", idleReturnDelayMs=" + idleReturnDelayMs + ", crystalPvp=" + crystalPvp + ", explosions=" + explosions + ", explosionBlockDamage=" + explosionBlockDamage + ", enderPearls=" + enderPearls + ", healing=" + healing + ", killMessageEnabled=" + killMessageEnabled + ", killMessage=" + killMessage + ", equipmentSlots=" + equipmentSlots + ", combatMode=" + combatMode + ", brain=" + brain + ", combatTuning=" + combatTuning + "]";
    }
}
