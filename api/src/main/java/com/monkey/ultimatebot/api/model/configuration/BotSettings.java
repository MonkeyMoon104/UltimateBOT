package com.monkey.ultimatebot.api.model.configuration;

import com.monkey.ultimatebot.api.model.identity.BotSkin;
import com.monkey.ultimatebot.api.model.runtime.BotLocation;
import com.monkey.ultimatebot.api.model.runtime.BotSpawnRequest;
import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.common.model.BrainKey;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Immutable settings payload used to configure bot behavior and editable limits.
 *
 * <p>Instances are created through a strict step-builder exposed by {@link #builder()}.
 * The builder enforces an explicit order and validates logical constraints such as:</p>
 * <ul>
 *     <li>{@code combat=true} automatically enables {@code follow}</li>
 *     <li>default values must stay inside their configured min/max ranges</li>
 *     <li>range boundaries must be coherent (min &lt;= max)</li>
 * </ul>
 *
 * <p>Use these settings in {@link BotSpawnRequest.Builder#settings(BotSettings)} to spawn bots.</p>
 */
public final class BotSettings {

    private final boolean follow;
    private final boolean combat;
    private final BotBlastProtection blastProtection;
    private final boolean changeableFollow;
    private final boolean changeableCombat;
    private final boolean changeableBlast;
    private final boolean changeableArmor;
    private final boolean changeableTotem;
    private final boolean changeableDifficulty;
    private final boolean changeableCombatMode;
    private final String botNameTemplate;
    private final BotSkin botSkin;
    private final BotArmorTier armorType;
    private final BotArmorTier minArmorType;
    private final BotArmorTier maxArmorType;
    private final int totemCount;
    private final int minTotemCount;
    private final int maxTotemCount;
    private final DifficultyTier difficulty;
    private final DifficultyTier minDifficulty;
    private final DifficultyTier maxDifficulty;
    private final CombatMode combatMode;
    private final @Nullable BrainKey brain;
    private final @Nullable CombatTuning combatTuning;
    private final @Nullable BotLocation spawnLocation;
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
    private final @Nullable String killMessage;
    private final Map<EquipmentSlotKind, ItemStack> armorContents;
    private final Map<Integer, ItemStack> equipmentContents;
    private final Map<EquipmentSlotKind, String> armorTrimPatternKeys;
    private final Map<EquipmentSlotKind, String> armorTrimMaterialKeys;

    private BotSettings(Builder builder) {
        this.follow = builder.follow;
        this.combat = builder.combat;
        this.blastProtection = builder.blastProtection;
        this.changeableFollow = builder.changeableFollow;
        this.changeableCombat = builder.changeableCombat;
        this.changeableBlast = builder.changeableBlast;
        this.changeableArmor = builder.changeableArmor;
        this.changeableTotem = builder.changeableTotem;
        this.changeableDifficulty = builder.changeableDifficulty;
        this.changeableCombatMode = builder.changeableCombatMode;
        this.botNameTemplate = builder.botNameTemplate;
        this.botSkin = builder.botSkin;
        this.armorType = builder.armorType;
        this.minArmorType = builder.minArmorType;
        this.maxArmorType = builder.maxArmorType;
        this.totemCount = builder.totemCount;
        this.minTotemCount = builder.minTotemCount;
        this.maxTotemCount = builder.maxTotemCount;
        this.difficulty = builder.difficulty;
        this.minDifficulty = builder.minDifficulty;
        this.maxDifficulty = builder.maxDifficulty;
        this.combatMode = builder.combatMode;
        this.brain = builder.brain;
        this.combatTuning = builder.combatTuning;
        this.spawnLocation = builder.spawnLocation;
        this.autoTarget = builder.autoTarget;
        this.autoTargetRange = builder.autoTargetRange;
        this.attackBots = builder.attackBots;
        this.targetMode = builder.targetMode;
        this.respectWorldGuardPvp = builder.respectWorldGuardPvp;
        this.stayAfterOwnerDeath = builder.stayAfterOwnerDeath;
        this.idleWander = builder.idleWander;
        this.idleWanderRadius = builder.idleWanderRadius;
        this.idleReturnDistance = builder.idleReturnDistance;
        this.idleReturnDelayMs = builder.idleReturnDelayMs;
        this.crystalPvp = builder.crystalPvp;
        this.explosions = builder.explosions;
        this.explosionBlockDamage = builder.explosionBlockDamage;
        this.enderPearls = builder.enderPearls;
        this.healing = builder.healing;
        this.killMessageEnabled = builder.killMessageEnabled;
        this.killMessage = builder.killMessage;
        this.armorContents = copyItemMap(builder.armorContents);
        this.equipmentContents = copyItemMap(builder.equipmentContents);
        this.armorTrimPatternKeys =
                com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(builder.armorTrimPatternKeys);
        this.armorTrimMaterialKeys =
                com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(builder.armorTrimMaterialKeys);
    }

    /**
     * Starts the mandatory ordered builder chain.
     *
     * @return first builder step (bot name)
     */
    public static BotNameStep builder() {
        return new Builder();
    }

    /**
     * Returns follow default state.
     *
     * @return follow default
     */
    public boolean follow() {
        return follow;
    }

    /**
     * Returns combat default state.
     *
     * @return combat default
     */
    public boolean combat() {
        return combat;
    }

    /**
     * Returns {@code true} only when blast protection is enabled on all armor pieces.
     *
     * @return global blast state derived from full profile
     */
    public boolean blastProtection() {
        return blastProtection.allEnabled();
    }

    /**
     * Returns full per-slot blast protection profile.
     *
     * @return blast protection profile
     */
    public BotBlastProtection blastProtectionProfile() {
        return blastProtection;
    }

    /**
     * Returns whether follow can be changed at runtime by users.
     *
     * @return follow mutability flag
     */
    public boolean changeableFollow() {
        return changeableFollow;
    }

    /**
     * Returns whether combat can be changed at runtime by users.
     *
     * @return combat mutability flag
     */
    public boolean changeableCombat() {
        return changeableCombat;
    }

    /**
     * Returns whether blast protection can be changed at runtime by users.
     *
     * @return blast mutability flag
     */
    public boolean changeableBlast() {
        return changeableBlast;
    }

    /**
     * Returns whether armor type can be changed at runtime by users.
     *
     * @return armor mutability flag
     */
    public boolean changeableArmor() {
        return changeableArmor;
    }

    /**
     * Returns whether totem count can be changed at runtime by users.
     *
     * @return totem mutability flag
     */
    public boolean changeableTotem() {
        return changeableTotem;
    }

    /**
     * Returns whether bot difficulty can be changed at runtime by users.
     *
     * @return difficulty mutability flag
     */
    public boolean changeableDifficulty() {
        return changeableDifficulty;
    }

    /**
     * Returns bot display-name template configured for spawn.
     *
     * @return bot name template
     */
    public String botNameTemplate() {
        return botNameTemplate;
    }

    /**
     * Returns skin strategy/configuration.
     *
     * @return bot skin config
     */
    public BotSkin botSkin() {
        return botSkin;
    }

    /**
     * Returns default armor type.
     *
     * @return default armor tier
     */
    public BotArmorTier armorType() {
        return armorType;
    }

    /**
     * Returns minimum allowed armor type.
     *
     * @return min armor tier
     */
    public BotArmorTier minArmorType() {
        return minArmorType;
    }

    /**
     * Returns maximum allowed armor type.
     *
     * @return max armor tier
     */
    public BotArmorTier maxArmorType() {
        return maxArmorType;
    }

    /**
     * Returns default totem count.
     *
     * @return default totem count
     */
    public int totemCount() {
        return totemCount;
    }

    /**
     * Returns minimum allowed totem count.
     *
     * @return min totem count
     */
    public int minTotemCount() {
        return minTotemCount;
    }

    /**
     * Returns maximum allowed totem count.
     *
     * @return max totem count
     */
    public int maxTotemCount() {
        return maxTotemCount;
    }

    /**
     * Returns default bot difficulty.
     *
     * @return default difficulty
     */
    public DifficultyTier difficulty() {
        return difficulty;
    }

    /**
     * Returns minimum allowed bot difficulty.
     *
     * @return min difficulty
     */
    public DifficultyTier minDifficulty() {
        return minDifficulty;
    }

    /**
     * Returns maximum allowed bot difficulty.
     *
     * @return max difficulty
     */
    public DifficultyTier maxDifficulty() {
        return maxDifficulty;
    }

    /** Returns the selected combat mode. */
    public CombatMode combatMode() {
        return combatMode;
    }

    /** Returns the custom brain selected at spawn, or {@code null} for the mode/default brain. */
    public @Nullable BrainKey brain() {
        return brain;
    }

    /** Returns a custom tuning override, or {@code null} to use the server profile. */
    public @Nullable CombatTuning combatTuning() {
        return combatTuning;
    }

    /** Returns whether players may change the combat mode from the bot GUI. */
    public boolean changeableCombatMode() {
        return changeableCombatMode;
    }

    public @Nullable BotLocation spawnLocation() {
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

    public @Nullable String killMessage() {
        return killMessage;
    }

    public Map<EquipmentSlotKind, ItemStack> armorContents() {
        return copyItemMap(armorContents);
    }

    public Map<Integer, ItemStack> equipmentContents() {
        return copyItemMap(equipmentContents);
    }

    public Map<EquipmentSlotKind, String> armorTrimPatternKeys() {
        return com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(armorTrimPatternKeys);
    }

    public Map<EquipmentSlotKind, String> armorTrimMaterialKeys() {
        return com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(armorTrimMaterialKeys);
    }

    /**
     * Step 1: choose bot name template.
     */
    public interface BotNameStep {
        /**
         * Sets the bot name template.
         *
         * <p>The template must not be blank. Placeholders supported by the core runtime
         * can be used (for example owner/target placeholders).</p>
         *
         * @param botNameTemplate bot name template
         * @return next step (skin configuration)
         */
        BotSkinStep setBotNameTemplate(String botNameTemplate);

        /**
         * Alias of {@link #setBotNameTemplate(String)}.
         *
         * @param botNameTemplate bot name template
         * @return next step (skin configuration)
         */
        default BotSkinStep setBotName(String botNameTemplate) {
            return setBotNameTemplate(botNameTemplate);
        }
    }

    /**
     * Step 2: choose bot skin source.
     */
    public interface BotSkinStep {
        /**
         * Sets an explicit skin configuration object.
         *
         * @param botSkin skin configuration
         * @return next step (follow default)
         */
        FollowStep setBotSkin(BotSkin botSkin);

        /**
         * Convenience skin selection.
         *
         * <p>{@code true} = owner skin, {@code false} = random skin.</p>
         *
         * @param useOwnerSkin whether owner skin should be used
         * @return next step (follow default)
         */
        FollowStep setBotSkin(boolean useOwnerSkin);

        /**
         * Uses random skin source.
         *
         * @return next step (follow default)
         */
        FollowStep setBotSkinRandom();

        /**
         * Uses owner skin source.
         *
         * @return next step (follow default)
         */
        FollowStep setBotSkinOwner();

        /**
         * Uses first TEAM_ALLY owner skin source.
         *
         * <p>This source is intended for TEAM_ALLY mode.</p>
         *
         * @return next step (follow default)
         */
        FollowStep setBotSkinFirstTeamOwner();

        /**
         * Uses skin from an online player reference.
         *
         * @param playerReference online player name or UUID string
         * @return next step (follow default)
         */
        FollowStep setBotSkinFromPlayerReference(String playerReference);

        /**
         * Uses raw texture payload without signature.
         *
         * @param textureValue Mojang texture value
         * @return next step (follow default)
         */
        FollowStep setBotSkinFromTextureValue(String textureValue);

        /**
         * Uses raw texture payload with optional signature.
         *
         * @param textureValue Mojang texture value
         * @param textureSignature Mojang texture signature
         * @return next step (follow default)
         */
        FollowStep setBotSkinFromTextureValue(String textureValue, String textureSignature);

        /**
         * Uses texture URL skin source.
         *
         * @param textureUrl http/https texture URL
         * @return next step (follow default)
         */
        FollowStep setBotSkinFromTextureUrl(String textureUrl);
    }

    /**
     * Step 3: set follow default.
     */
    public interface FollowStep {
        /**
         * Sets follow default state.
         *
         * @param followEnabled follow default
         * @return next step (follow mutability)
         */
        ChangeableFollowStep follow(boolean followEnabled);
    }

    /**
     * Step 4: configure follow mutability.
     */
    public interface ChangeableFollowStep {
        /**
         * Sets whether follow can be changed later.
         *
         * @param followChangeable follow changeability
         * @return next step (combat default)
         */
        CombatStep setChangeableFollow(boolean followChangeable);
    }

    /**
     * Step 5: set combat default.
     */
    public interface CombatStep {
        /**
         * Sets combat default state.
         *
         * <p>Build-time normalization enables follow when {@code combat=true}.</p>
         *
         * @param combatEnabled combat default
         * @return next step (combat mutability)
         */
        ChangeableCombatStep combat(boolean combatEnabled);
    }

    /**
     * Step 6: configure combat mutability.
     */
    public interface ChangeableCombatStep {
        /**
         * Sets whether combat can be changed later.
         *
         * @param combatChangeable combat changeability
         * @return next step (blast profile)
         */
        BlastProtectionStep setChangeableCombat(boolean combatChangeable);
    }

    /**
     * Step 7: configure blast protection profile.
     */
    public interface BlastProtectionStep {
        /**
         * Sets per-slot blast profile using binary integers ({@code 0}/{@code 1}).
         *
         * @param bootsBlastEnabled boots blast state (0/1)
         * @param leggingsBlastEnabled leggings blast state (0/1)
         * @param chestplateBlastEnabled chestplate blast state (0/1)
         * @param helmetBlastEnabled helmet blast state (0/1)
         * @return next step (blast mutability)
         */
        ChangeableBlastStep blastProtection(
                int bootsBlastEnabled, int leggingsBlastEnabled, int chestplateBlastEnabled, int helmetBlastEnabled);

        /**
         * Sets per-slot blast profile using booleans.
         *
         * @param bootsBlastEnabled boots blast state
         * @param leggingsBlastEnabled leggings blast state
         * @param chestplateBlastEnabled chestplate blast state
         * @param helmetBlastEnabled helmet blast state
         * @return next step (blast mutability)
         */
        ChangeableBlastStep blastProtection(
                boolean bootsBlastEnabled,
                boolean leggingsBlastEnabled,
                boolean chestplateBlastEnabled,
                boolean helmetBlastEnabled);

        /**
         * Sets explicit blast protection profile object.
         *
         * @param blastProtectionProfile blast profile
         * @return next step (blast mutability)
         */
        ChangeableBlastStep blastProtection(BotBlastProtection blastProtectionProfile);
    }

    /**
     * Step 8: configure blast mutability.
     */
    public interface ChangeableBlastStep {
        /**
         * Sets whether blast protection can be changed later.
         *
         * @param blastChangeable blast changeability
         * @return next step (armor range)
         */
        ArmorRangeStep setChangeableBlast(boolean blastChangeable);
    }

    /**
     * Step 9: configure allowed armor range.
     */
    public interface ArmorRangeStep {
        /**
         * Sets minimum and maximum allowed armor tier.
         *
         * @param minArmorType minimum armor tier
         * @param maxArmorType maximum armor tier
         * @return next step (default armor)
         */
        ArmorStep armorValue(BotArmorTier minArmorType, BotArmorTier maxArmorType);
    }

    /**
     * Step 10: configure default armor.
     */
    public interface ArmorStep {
        /**
         * Sets default armor tier.
         *
         * @param defaultArmorType default armor tier
         * @return next step (armor mutability)
         */
        ChangeableArmorStep armor(BotArmorTier defaultArmorType);
    }

    /**
     * Step 11: configure armor mutability.
     */
    public interface ChangeableArmorStep {
        /**
         * Sets whether armor can be changed later.
         *
         * @param armorChangeable armor changeability
         * @return next step (totem range)
         */
        TotemRangeStep setChangeableArmor(boolean armorChangeable);
    }

    /**
     * Step 12: configure allowed totem range.
     */
    public interface TotemRangeStep {
        /**
         * Sets minimum and maximum allowed totem count.
         *
         * <p>{@code -1} is reserved for unlimited and is valid only as minimum.
         * Maximum must be {@code >= 0}.</p>
         *
         * @param minTotemCount minimum allowed totems
         * @param maxTotemCount maximum allowed totems
         * @return next step (default totems)
         */
        TotemStep totemValue(int minTotemCount, int maxTotemCount);
    }

    /**
     * Step 13: configure default totem count.
     */
    public interface TotemStep {
        /**
         * Sets default totem count.
         *
         * @param defaultTotemCount default totem count
         * @return next step (totem mutability)
         */
        ChangeableTotemStep totemCount(int defaultTotemCount);
    }

    /**
     * Step 14: configure totem mutability.
     */
    public interface ChangeableTotemStep {
        /**
         * Sets whether totem count can be changed later.
         *
         * @param totemChangeable totem changeability
         * @return next step (difficulty range)
         */
        DifficultyRangeStep setChangeableTotem(boolean totemChangeable);
    }

    /**
     * Step 15: configure allowed difficulty range.
     */
    public interface DifficultyRangeStep {
        /**
         * Sets minimum and maximum allowed difficulty.
         *
         * @param minDifficulty minimum difficulty
         * @param maxDifficulty maximum difficulty
         * @return next step (default difficulty)
         */
        DifficultyStep difficultyValue(DifficultyTier minDifficulty, DifficultyTier maxDifficulty);
    }

    /**
     * Step 16: configure default difficulty.
     */
    public interface DifficultyStep {
        /**
         * Sets default difficulty.
         *
         * @param defaultDifficulty default difficulty
         * @return next step (difficulty mutability)
         */
        ChangeableDifficultyStep difficulty(DifficultyTier defaultDifficulty);
    }

    /**
     * Step 17: configure difficulty mutability.
     */
    public interface ChangeableDifficultyStep {
        /**
         * Sets whether difficulty can be changed later.
         *
         * @param difficultyChangeable difficulty changeability
         * @return final build step
         */
        BuildStep setChangeableDifficulty(boolean difficultyChangeable);
    }

    /**
     * Final step: builds immutable settings.
     */
    public interface BuildStep {
        BuildStep spawnLocation(@Nullable BotLocation spawnLocation);

        BuildStep autoTarget(boolean autoTarget);

        BuildStep autoTargetRange(double autoTargetRange);

        BuildStep attackBots(boolean attackBots);

        BuildStep targetMode(BotTargetMode targetMode);

        BuildStep combatMode(CombatMode combatMode);

        BuildStep brain(@Nullable BrainKey brain);

        BuildStep combatTuning(@Nullable CombatTuning combatTuning);

        BuildStep changeableCombatMode(boolean changeableCombatMode);

        BuildStep respectWorldGuardPvp(boolean respectWorldGuardPvp);

        BuildStep stayAfterOwnerDeath(boolean stayAfterOwnerDeath);

        BuildStep idleWander(boolean idleWander);

        BuildStep idleWanderRadius(double idleWanderRadius);

        BuildStep idleReturnDistance(double idleReturnDistance);

        BuildStep idleReturnDelayMs(long idleReturnDelayMs);

        BuildStep crystalPvp(boolean crystalPvp);

        BuildStep explosions(boolean explosions);

        BuildStep explosionBlockDamage(boolean explosionBlockDamage);

        BuildStep enderPearls(boolean enderPearls);

        BuildStep healing(boolean healing);

        BuildStep disableHealing();

        BuildStep killMessage(@Nullable String killMessage);

        BuildStep disableKillMessage();

        BuildStep armorContents(Map<EquipmentSlotKind, ItemStack> armorContents);

        BuildStep equipmentContents(Map<Integer, ItemStack> equipmentContents);

        BuildStep armorTrim(
                @Nullable EquipmentSlotKind slot, @Nullable String patternKey, @Nullable String materialKey);

        /**
         * Builds immutable settings after validation.
         *
         * @return immutable bot settings
         * @throws IllegalArgumentException if any configured rule is invalid or inconsistent
         */
        BotSettings build();
    }

    private static final class Builder
            implements BotNameStep,
                    BotSkinStep,
                    FollowStep,
                    ChangeableFollowStep,
                    CombatStep,
                    ChangeableCombatStep,
                    BlastProtectionStep,
                    ChangeableBlastStep,
                    ArmorRangeStep,
                    ArmorStep,
                    ChangeableArmorStep,
                    TotemRangeStep,
                    TotemStep,
                    ChangeableTotemStep,
                    DifficultyRangeStep,
                    DifficultyStep,
                    ChangeableDifficultyStep,
                    BuildStep {
        private boolean follow;
        private boolean combat;
        private BotBlastProtection blastProtection = BotBlastProtection.all(false);
        private boolean changeableFollow;
        private boolean changeableCombat;
        private boolean changeableBlast;
        private boolean changeableArmor;
        private boolean changeableTotem;
        private boolean changeableDifficulty;
        private boolean changeableCombatMode = true;
        private String botNameTemplate = "";
        private BotSkin botSkin = BotSkin.random();
        private BotArmorTier armorType = BotArmorTier.LEATHER;
        private BotArmorTier minArmorType = BotArmorTier.LEATHER;
        private BotArmorTier maxArmorType = BotArmorTier.NETHERITE;
        private int totemCount;
        private int minTotemCount;
        private int maxTotemCount;
        private DifficultyTier difficulty = DifficultyTier.EASY;
        private DifficultyTier minDifficulty = DifficultyTier.EASY;
        private DifficultyTier maxDifficulty = DifficultyTier.GOD;
        private CombatMode combatMode = CombatMode.SWORD;
        private @Nullable BrainKey brain;
        private @Nullable CombatTuning combatTuning;
        private @Nullable BotLocation spawnLocation;
        private boolean autoTarget = false;
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
        private final Map<EquipmentSlotKind, ItemStack> armorContents = new EnumMap<>(EquipmentSlotKind.class);
        private final Map<Integer, ItemStack> equipmentContents = new HashMap<>();
        private final Map<EquipmentSlotKind, String> armorTrimPatternKeys = new EnumMap<>(EquipmentSlotKind.class);
        private final Map<EquipmentSlotKind, String> armorTrimMaterialKeys = new EnumMap<>(EquipmentSlotKind.class);

        private Builder() {}

        @Override
        public BotSkinStep setBotNameTemplate(String botNameTemplate) {
            this.botNameTemplate =
                    Objects.requireNonNull(botNameTemplate, "botNameTemplate").trim();
            return this;
        }

        @Override
        public FollowStep setBotSkin(BotSkin botSkin) {
            this.botSkin = Objects.requireNonNull(botSkin, "botSkin");
            return this;
        }

        @Override
        public FollowStep setBotSkin(boolean useOwnerSkin) {
            this.botSkin = useOwnerSkin ? BotSkin.owner() : BotSkin.random();
            return this;
        }

        @Override
        public FollowStep setBotSkinRandom() {
            this.botSkin = BotSkin.random();
            return this;
        }

        @Override
        public FollowStep setBotSkinOwner() {
            this.botSkin = BotSkin.owner();
            return this;
        }

        @Override
        public FollowStep setBotSkinFirstTeamOwner() {
            this.botSkin = BotSkin.firstTeamOwner();
            return this;
        }

        @Override
        public FollowStep setBotSkinFromPlayerReference(String playerReference) {
            this.botSkin = BotSkin.player(playerReference);
            return this;
        }

        @Override
        public FollowStep setBotSkinFromTextureValue(String textureValue) {
            this.botSkin = BotSkin.texture(textureValue);
            return this;
        }

        @Override
        public FollowStep setBotSkinFromTextureValue(String textureValue, String textureSignature) {
            this.botSkin = BotSkin.texture(textureValue, textureSignature);
            return this;
        }

        @Override
        public FollowStep setBotSkinFromTextureUrl(String textureUrl) {
            this.botSkin = BotSkin.url(textureUrl);
            return this;
        }

        @Override
        public ChangeableFollowStep follow(boolean followEnabled) {
            this.follow = followEnabled;
            return this;
        }

        @Override
        public CombatStep setChangeableFollow(boolean followChangeable) {
            this.changeableFollow = followChangeable;
            return this;
        }

        @Override
        public ChangeableCombatStep combat(boolean combatEnabled) {
            this.combat = combatEnabled;
            return this;
        }

        @Override
        public BlastProtectionStep setChangeableCombat(boolean combatChangeable) {
            this.changeableCombat = combatChangeable;
            return this;
        }

        @Override
        public ChangeableBlastStep blastProtection(
                int bootsBlastEnabled, int leggingsBlastEnabled, int chestplateBlastEnabled, int helmetBlastEnabled) {
            this.blastProtection = BotBlastProtection.of(
                    bootsBlastEnabled, leggingsBlastEnabled, chestplateBlastEnabled, helmetBlastEnabled);
            return this;
        }

        @Override
        public ChangeableBlastStep blastProtection(
                boolean bootsBlastEnabled,
                boolean leggingsBlastEnabled,
                boolean chestplateBlastEnabled,
                boolean helmetBlastEnabled) {
            this.blastProtection = BotBlastProtection.of(
                    bootsBlastEnabled, leggingsBlastEnabled, chestplateBlastEnabled, helmetBlastEnabled);
            return this;
        }

        @Override
        public ChangeableBlastStep blastProtection(BotBlastProtection blastProtectionProfile) {
            this.blastProtection = Objects.requireNonNull(blastProtectionProfile, "blastProtectionProfile");
            return this;
        }

        @Override
        public ArmorRangeStep setChangeableBlast(boolean blastChangeable) {
            this.changeableBlast = blastChangeable;
            return this;
        }

        @Override
        public ArmorStep armorValue(BotArmorTier minArmorType, BotArmorTier maxArmorType) {
            this.minArmorType = Objects.requireNonNull(minArmorType, "minArmorType");
            this.maxArmorType = Objects.requireNonNull(maxArmorType, "maxArmorType");
            return this;
        }

        @Override
        public ChangeableArmorStep armor(BotArmorTier defaultArmorType) {
            this.armorType = Objects.requireNonNull(defaultArmorType, "defaultArmorType");
            return this;
        }

        @Override
        public TotemRangeStep setChangeableArmor(boolean armorChangeable) {
            this.changeableArmor = armorChangeable;
            return this;
        }

        @Override
        public TotemStep totemValue(int minTotemCount, int maxTotemCount) {
            this.minTotemCount = minTotemCount;
            this.maxTotemCount = maxTotemCount;
            return this;
        }

        @Override
        public ChangeableTotemStep totemCount(int defaultTotemCount) {
            this.totemCount = defaultTotemCount;
            return this;
        }

        @Override
        public DifficultyRangeStep setChangeableTotem(boolean totemChangeable) {
            this.changeableTotem = totemChangeable;
            return this;
        }

        @Override
        public DifficultyStep difficultyValue(DifficultyTier minDifficulty, DifficultyTier maxDifficulty) {
            this.minDifficulty = Objects.requireNonNull(minDifficulty, "minDifficulty");
            this.maxDifficulty = Objects.requireNonNull(maxDifficulty, "maxDifficulty");
            return this;
        }

        @Override
        public ChangeableDifficultyStep difficulty(DifficultyTier defaultDifficulty) {
            this.difficulty = Objects.requireNonNull(defaultDifficulty, "defaultDifficulty");
            return this;
        }

        @Override
        public BuildStep setChangeableDifficulty(boolean difficultyChangeable) {
            this.changeableDifficulty = difficultyChangeable;
            return this;
        }

        @Override
        public BuildStep spawnLocation(@Nullable BotLocation spawnLocation) {
            this.spawnLocation = spawnLocation;
            return this;
        }

        @Override
        public BuildStep autoTarget(boolean autoTarget) {
            this.autoTarget = autoTarget;
            return this;
        }

        @Override
        public BuildStep autoTargetRange(double autoTargetRange) {
            this.autoTargetRange = autoTargetRange;
            return this;
        }

        @Override
        public BuildStep attackBots(boolean attackBots) {
            this.attackBots = attackBots;
            return this;
        }

        @Override
        public BuildStep targetMode(BotTargetMode targetMode) {
            this.targetMode = Objects.requireNonNull(targetMode, "targetMode");
            return this;
        }

        @Override
        public BuildStep combatMode(CombatMode combatMode) {
            this.combatMode = Objects.requireNonNull(combatMode, "combatMode");
            return this;
        }

        @Override
        public BuildStep brain(@Nullable BrainKey brain) {
            this.brain = brain;
            return this;
        }

        @Override
        public BuildStep combatTuning(@Nullable CombatTuning combatTuning) {
            this.combatTuning = combatTuning;
            return this;
        }

        @Override
        public BuildStep changeableCombatMode(boolean changeableCombatMode) {
            this.changeableCombatMode = changeableCombatMode;
            return this;
        }

        @Override
        public BuildStep respectWorldGuardPvp(boolean respectWorldGuardPvp) {
            this.respectWorldGuardPvp = respectWorldGuardPvp;
            return this;
        }

        @Override
        public BuildStep stayAfterOwnerDeath(boolean stayAfterOwnerDeath) {
            this.stayAfterOwnerDeath = stayAfterOwnerDeath;
            return this;
        }

        @Override
        public BuildStep idleWander(boolean idleWander) {
            this.idleWander = idleWander;
            return this;
        }

        @Override
        public BuildStep idleWanderRadius(double idleWanderRadius) {
            this.idleWanderRadius = idleWanderRadius;
            return this;
        }

        @Override
        public BuildStep idleReturnDistance(double idleReturnDistance) {
            this.idleReturnDistance = idleReturnDistance;
            return this;
        }

        @Override
        public BuildStep idleReturnDelayMs(long idleReturnDelayMs) {
            this.idleReturnDelayMs = idleReturnDelayMs;
            return this;
        }

        @Override
        public BuildStep crystalPvp(boolean crystalPvp) {
            this.crystalPvp = crystalPvp;
            return this;
        }

        @Override
        public BuildStep explosions(boolean explosions) {
            this.explosions = explosions;
            return this;
        }

        @Override
        public BuildStep explosionBlockDamage(boolean explosionBlockDamage) {
            this.explosionBlockDamage = explosionBlockDamage;
            return this;
        }

        @Override
        public BuildStep enderPearls(boolean enderPearls) {
            this.enderPearls = enderPearls;
            return this;
        }

        @Override
        public BuildStep healing(boolean healing) {
            this.healing = healing;
            return this;
        }

        @Override
        public BuildStep disableHealing() {
            this.healing = false;
            return this;
        }

        @Override
        public BuildStep killMessage(@Nullable String killMessage) {
            this.killMessageEnabled = true;
            this.killMessage = killMessage;
            return this;
        }

        @Override
        public BuildStep disableKillMessage() {
            this.killMessageEnabled = false;
            this.killMessage = null;
            return this;
        }

        @Override
        public BuildStep armorContents(Map<EquipmentSlotKind, ItemStack> armorContents) {
            this.armorContents.clear();
            this.armorContents.putAll(copyItemMap(armorContents));
            return this;
        }

        @Override
        public BuildStep equipmentContents(Map<Integer, ItemStack> equipmentContents) {
            this.equipmentContents.clear();
            this.equipmentContents.putAll(copyItemMap(equipmentContents));
            return this;
        }

        @Override
        public BuildStep armorTrim(
                @Nullable EquipmentSlotKind slot, @Nullable String patternKey, @Nullable String materialKey) {
            if (slot != null) {
                if (patternKey == null || patternKey.trim().isEmpty()) {
                    this.armorTrimPatternKeys.remove(slot);
                } else {
                    this.armorTrimPatternKeys.put(slot, patternKey);
                }
                if (materialKey == null || materialKey.trim().isEmpty()) {
                    this.armorTrimMaterialKeys.remove(slot);
                } else {
                    this.armorTrimMaterialKeys.put(slot, materialKey);
                }
            }
            return this;
        }

        @Override
        public BotSettings build() {
            Objects.requireNonNull(blastProtection, "blastProtection");
            Objects.requireNonNull(armorType, "armorType");
            Objects.requireNonNull(minArmorType, "minArmorType");
            Objects.requireNonNull(maxArmorType, "maxArmorType");
            Objects.requireNonNull(difficulty, "difficulty");
            Objects.requireNonNull(minDifficulty, "minDifficulty");
            Objects.requireNonNull(maxDifficulty, "maxDifficulty");
            Objects.requireNonNull(combatMode, "combatMode");
            Objects.requireNonNull(botSkin, "botSkin");
            if (botNameTemplate == null || botNameTemplate.trim().isEmpty()) {
                throw new IllegalArgumentException("botNameTemplate cannot be blank");
            }
            validateArmorRange(minArmorType, maxArmorType);
            validateTotemRange(minTotemCount, maxTotemCount);
            validateDifficultyRange(minDifficulty, maxDifficulty);

            if (armorType.compareTo(minArmorType) < 0 || armorType.compareTo(maxArmorType) > 0) {
                throw new IllegalArgumentException(
                        "armor must be within range [" + minArmorType + ", " + maxArmorType + "]");
            }

            if (totemCount < minTotemCount || totemCount > maxTotemCount) {
                if (totemCount != -1 || minTotemCount > -1) {
                    throw new IllegalArgumentException(
                            "totemCount must be within range [" + minTotemCount + ", " + maxTotemCount + "]");
                }
            }

            if (difficulty.compareTo(minDifficulty) < 0 || difficulty.compareTo(maxDifficulty) > 0) {
                throw new IllegalArgumentException(
                        "difficulty must be within range [" + minDifficulty + ", " + maxDifficulty + "]");
            }

            if (combat && !follow) {
                follow = true;
            }

            if (!follow && !changeableFollow && changeableCombat) {
                throw new IllegalArgumentException("combat cannot be changeable when follow is fixed to false");
            }

            if (combat && !changeableCombat && changeableFollow) {
                throw new IllegalArgumentException("follow cannot be changeable while combat is fixed to true");
            }

            if (autoTargetRange <= 0.0D) {
                throw new IllegalArgumentException("autoTargetRange must be greater than 0");
            }

            if (idleWanderRadius <= 0.0D) {
                throw new IllegalArgumentException("idleWanderRadius must be greater than 0");
            }

            if (idleReturnDistance <= 0.0D) {
                throw new IllegalArgumentException("idleReturnDistance must be greater than 0");
            }

            if (idleReturnDelayMs < 0L) {
                throw new IllegalArgumentException("idleReturnDelayMs cannot be negative");
            }

            return new BotSettings(this);
        }

        private static void validateTotemRange(int min, int max) {
            if (min < -1) {
                throw new IllegalArgumentException("min totem cannot be less than -1");
            }
            if (max < 0) {
                throw new IllegalArgumentException("max totem must be >= 0");
            }
            if (min > max) {
                throw new IllegalArgumentException("min totem cannot be greater than max totem");
            }
        }

        private static void validateArmorRange(BotArmorTier minArmorType, BotArmorTier maxArmorType) {
            if (minArmorType.compareTo(maxArmorType) > 0) {
                throw new IllegalArgumentException("min armor cannot be greater than max armor");
            }
        }

        private static void validateDifficultyRange(DifficultyTier min, DifficultyTier max) {
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("min difficulty cannot be greater than max difficulty");
            }
        }
    }

    private static <K> Map<K, ItemStack> copyItemMap(@Nullable Map<K, ItemStack> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, ItemStack> copy = new HashMap<>();
        for (Map.Entry<K, ItemStack> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                copy.put(entry.getKey(), entry.getValue().clone());
            }
        }
        return com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(copy);
    }
}
