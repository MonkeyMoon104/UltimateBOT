package com.monkey.mcbot.api.model;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.EquipmentSlot;
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
    private final boolean changeableRank;
    private final String botNameTemplate;
    private final BotSkin botSkin;
    private final BotArmorType armorType;
    private final BotArmorType minArmorType;
    private final BotArmorType maxArmorType;
    private final int totemCount;
    private final int minTotemCount;
    private final int maxTotemCount;
    private final BotRank rank;
    private final BotRank minRank;
    private final BotRank maxRank;
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
    private final Map<EquipmentSlot, ItemStack> armorContents;
    private final Map<Integer, ItemStack> equipmentContents;
    private final Map<EquipmentSlot, String> armorTrimPatternKeys;
    private final Map<EquipmentSlot, String> armorTrimMaterialKeys;

    private BotSettings(Builder builder) {
        this.follow = builder.follow;
        this.combat = builder.combat;
        this.blastProtection = builder.blastProtection;
        this.changeableFollow = builder.changeableFollow;
        this.changeableCombat = builder.changeableCombat;
        this.changeableBlast = builder.changeableBlast;
        this.changeableArmor = builder.changeableArmor;
        this.changeableTotem = builder.changeableTotem;
        this.changeableRank = builder.changeableRank;
        this.botNameTemplate = builder.botNameTemplate;
        this.botSkin = builder.botSkin;
        this.armorType = builder.armorType;
        this.minArmorType = builder.minArmorType;
        this.maxArmorType = builder.maxArmorType;
        this.totemCount = builder.totemCount;
        this.minTotemCount = builder.minTotemCount;
        this.maxTotemCount = builder.maxTotemCount;
        this.rank = builder.rank;
        this.minRank = builder.minRank;
        this.maxRank = builder.maxRank;
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
        this.armorTrimPatternKeys = Map.copyOf(builder.armorTrimPatternKeys);
        this.armorTrimMaterialKeys = Map.copyOf(builder.armorTrimMaterialKeys);
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
     * Returns whether bot rank can be changed at runtime by users.
     *
     * @return rank mutability flag
     */
    public boolean changeableRank() {
        return changeableRank;
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
    public BotArmorType armorType() {
        return armorType;
    }

    /**
     * Returns minimum allowed armor type.
     *
     * @return min armor tier
     */
    public BotArmorType minArmorType() {
        return minArmorType;
    }

    /**
     * Returns maximum allowed armor type.
     *
     * @return max armor tier
     */
    public BotArmorType maxArmorType() {
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
     * Returns default bot rank.
     *
     * @return default rank
     */
    public BotRank rank() {
        return rank;
    }

    /**
     * Returns minimum allowed bot rank.
     *
     * @return min rank
     */
    public BotRank minRank() {
        return minRank;
    }

    /**
     * Returns maximum allowed bot rank.
     *
     * @return max rank
     */
    public BotRank maxRank() {
        return maxRank;
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

    public Map<EquipmentSlot, ItemStack> armorContents() {
        return copyItemMap(armorContents);
    }

    public Map<Integer, ItemStack> equipmentContents() {
        return copyItemMap(equipmentContents);
    }

    public Map<EquipmentSlot, String> armorTrimPatternKeys() {
        return Map.copyOf(armorTrimPatternKeys);
    }

    public Map<EquipmentSlot, String> armorTrimMaterialKeys() {
        return Map.copyOf(armorTrimMaterialKeys);
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
        ArmorStep armorValue(BotArmorType minArmorType, BotArmorType maxArmorType);
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
        ChangeableArmorStep armor(BotArmorType defaultArmorType);
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
         * @return next step (rank range)
         */
        RankRangeStep setChangeableTotem(boolean totemChangeable);
    }

    /**
     * Step 15: configure allowed rank range.
     */
    public interface RankRangeStep {
        /**
         * Sets minimum and maximum allowed rank.
         *
         * @param minRank minimum rank
         * @param maxRank maximum rank
         * @return next step (default rank)
         */
        RankStep rankValue(BotRank minRank, BotRank maxRank);
    }

    /**
     * Step 16: configure default rank.
     */
    public interface RankStep {
        /**
         * Sets default rank.
         *
         * @param defaultRank default rank
         * @return next step (rank mutability)
         */
        ChangeableRankStep rank(BotRank defaultRank);
    }

    /**
     * Step 17: configure rank mutability.
     */
    public interface ChangeableRankStep {
        /**
         * Sets whether rank can be changed later.
         *
         * @param rankChangeable rank changeability
         * @return final build step
         */
        BuildStep setChangeableRank(boolean rankChangeable);
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

        BuildStep armorContents(Map<EquipmentSlot, ItemStack> armorContents);

        BuildStep equipmentContents(Map<Integer, ItemStack> equipmentContents);

        BuildStep armorTrim(@Nullable EquipmentSlot slot, @Nullable String patternKey, @Nullable String materialKey);

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
                    RankRangeStep,
                    RankStep,
                    ChangeableRankStep,
                    BuildStep {
        private boolean follow;
        private boolean combat;
        private BotBlastProtection blastProtection = BotBlastProtection.all(false);
        private boolean changeableFollow;
        private boolean changeableCombat;
        private boolean changeableBlast;
        private boolean changeableArmor;
        private boolean changeableTotem;
        private boolean changeableRank;
        private String botNameTemplate = "";
        private BotSkin botSkin = BotSkin.random();
        private BotArmorType armorType = BotArmorType.LEATHER;
        private BotArmorType minArmorType = BotArmorType.LEATHER;
        private BotArmorType maxArmorType = BotArmorType.NETHERITE;
        private int totemCount;
        private int minTotemCount;
        private int maxTotemCount;
        private BotRank rank = BotRank.EASY;
        private BotRank minRank = BotRank.EASY;
        private BotRank maxRank = BotRank.GOD;
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
        private final Map<EquipmentSlot, ItemStack> armorContents = new EnumMap<>(EquipmentSlot.class);
        private final Map<Integer, ItemStack> equipmentContents = new HashMap<>();
        private final Map<EquipmentSlot, String> armorTrimPatternKeys = new EnumMap<>(EquipmentSlot.class);
        private final Map<EquipmentSlot, String> armorTrimMaterialKeys = new EnumMap<>(EquipmentSlot.class);

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
        public ArmorStep armorValue(BotArmorType minArmorType, BotArmorType maxArmorType) {
            this.minArmorType = Objects.requireNonNull(minArmorType, "minArmorType");
            this.maxArmorType = Objects.requireNonNull(maxArmorType, "maxArmorType");
            return this;
        }

        @Override
        public ChangeableArmorStep armor(BotArmorType defaultArmorType) {
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
        public RankRangeStep setChangeableTotem(boolean totemChangeable) {
            this.changeableTotem = totemChangeable;
            return this;
        }

        @Override
        public RankStep rankValue(BotRank minRank, BotRank maxRank) {
            this.minRank = Objects.requireNonNull(minRank, "minRank");
            this.maxRank = Objects.requireNonNull(maxRank, "maxRank");
            return this;
        }

        @Override
        public ChangeableRankStep rank(BotRank defaultRank) {
            this.rank = Objects.requireNonNull(defaultRank, "defaultRank");
            return this;
        }

        @Override
        public BuildStep setChangeableRank(boolean rankChangeable) {
            this.changeableRank = rankChangeable;
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
        public BuildStep armorContents(Map<EquipmentSlot, ItemStack> armorContents) {
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
                @Nullable EquipmentSlot slot, @Nullable String patternKey, @Nullable String materialKey) {
            if (slot != null) {
                if (patternKey == null || patternKey.isBlank()) {
                    this.armorTrimPatternKeys.remove(slot);
                } else {
                    this.armorTrimPatternKeys.put(slot, patternKey);
                }
                if (materialKey == null || materialKey.isBlank()) {
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
            Objects.requireNonNull(rank, "rank");
            Objects.requireNonNull(minRank, "minRank");
            Objects.requireNonNull(maxRank, "maxRank");
            Objects.requireNonNull(botSkin, "botSkin");
            if (botNameTemplate == null || botNameTemplate.isBlank()) {
                throw new IllegalArgumentException("botNameTemplate cannot be blank");
            }
            validateArmorRange(minArmorType, maxArmorType);
            validateTotemRange(minTotemCount, maxTotemCount);
            validateRankRange(minRank, maxRank);

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

            if (rank.compareTo(minRank) < 0 || rank.compareTo(maxRank) > 0) {
                throw new IllegalArgumentException("rank must be within range [" + minRank + ", " + maxRank + "]");
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

        private static void validateArmorRange(BotArmorType minArmorType, BotArmorType maxArmorType) {
            if (minArmorType.compareTo(maxArmorType) > 0) {
                throw new IllegalArgumentException("min armor cannot be greater than max armor");
            }
        }

        private static void validateRankRange(BotRank min, BotRank max) {
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("min rank cannot be greater than max rank");
            }
        }
    }

    private static <K> Map<K, ItemStack> copyItemMap(@Nullable Map<K, ItemStack> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<K, ItemStack> copy = new HashMap<>();
        for (Map.Entry<K, ItemStack> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                copy.put(entry.getKey(), entry.getValue().clone());
            }
        }
        return Map.copyOf(copy);
    }
}
