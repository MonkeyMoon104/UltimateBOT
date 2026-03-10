package com.monkey.mcbot.api.model;

import java.util.Objects;

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
    }

    public static BotNameStep builder() {
        return new Builder();
    }

    public boolean follow() {
        return follow;
    }

    public boolean combat() {
        return combat;
    }

    public boolean blastProtection() {
        return blastProtection.allEnabled();
    }

    public BotBlastProtection blastProtectionProfile() {
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

    public boolean changeableRank() {
        return changeableRank;
    }

    public String botNameTemplate() {
        return botNameTemplate;
    }

    public BotSkin botSkin() {
        return botSkin;
    }

    public BotArmorType armorType() {
        return armorType;
    }

    public BotArmorType minArmorType() {
        return minArmorType;
    }

    public BotArmorType maxArmorType() {
        return maxArmorType;
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

    public BotRank rank() {
        return rank;
    }

    public BotRank minRank() {
        return minRank;
    }

    public BotRank maxRank() {
        return maxRank;
    }

    public interface BotNameStep {
        BotSkinStep setBotNameTemplate(String botNameTemplate);

        default BotSkinStep setBotName(String botNameTemplate) {
            return setBotNameTemplate(botNameTemplate);
        }
    }

    public interface BotSkinStep {
        FollowStep setBotSkin(BotSkin botSkin);

        FollowStep setBotSkin(boolean useOwnerSkin);

        FollowStep setBotSkinRandom();

        FollowStep setBotSkinOwner();

        FollowStep setBotSkinFirstTeamOwner();

        FollowStep setBotSkinFromPlayerReference(String playerReference);

        FollowStep setBotSkinFromTextureValue(String textureValue);

        FollowStep setBotSkinFromTextureValue(String textureValue, String textureSignature);

        FollowStep setBotSkinFromTextureUrl(String textureUrl);
    }

    public interface FollowStep {
        ChangeableFollowStep follow(boolean followEnabled);
    }

    public interface ChangeableFollowStep {
        CombatStep setChangeableFollow(boolean followChangeable);
    }

    public interface CombatStep {
        ChangeableCombatStep combat(boolean combatEnabled);
    }

    public interface ChangeableCombatStep {
        BlastProtectionStep setChangeableCombat(boolean combatChangeable);
    }

    public interface BlastProtectionStep {
        ChangeableBlastStep blastProtection(
                int bootsBlastEnabled,
                int leggingsBlastEnabled,
                int chestplateBlastEnabled,
                int helmetBlastEnabled
        );

        ChangeableBlastStep blastProtection(
                boolean bootsBlastEnabled,
                boolean leggingsBlastEnabled,
                boolean chestplateBlastEnabled,
                boolean helmetBlastEnabled
        );

        ChangeableBlastStep blastProtection(BotBlastProtection blastProtectionProfile);
    }

    public interface ChangeableBlastStep {
        ArmorRangeStep setChangeableBlast(boolean blastChangeable);
    }

    public interface ArmorRangeStep {
        ArmorStep armorValue(BotArmorType minArmorType, BotArmorType maxArmorType);
    }

    public interface ArmorStep {
        ChangeableArmorStep armor(BotArmorType defaultArmorType);
    }

    public interface ChangeableArmorStep {
        TotemRangeStep setChangeableArmor(boolean armorChangeable);
    }

    public interface TotemRangeStep {
        TotemStep totemValue(int minTotemCount, int maxTotemCount);
    }

    public interface TotemStep {
        ChangeableTotemStep totemCount(int defaultTotemCount);
    }

    public interface ChangeableTotemStep {
        RankRangeStep setChangeableTotem(boolean totemChangeable);
    }

    public interface RankRangeStep {
        RankStep rankValue(BotRank minRank, BotRank maxRank);
    }

    public interface RankStep {
        ChangeableRankStep rank(BotRank defaultRank);
    }

    public interface ChangeableRankStep {
        BuildStep setChangeableRank(boolean rankChangeable);
    }

    public interface BuildStep {
        BotSettings build();
    }

    private static final class Builder implements BotNameStep,
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
        private BotBlastProtection blastProtection;
        private boolean changeableFollow;
        private boolean changeableCombat;
        private boolean changeableBlast;
        private boolean changeableArmor;
        private boolean changeableTotem;
        private boolean changeableRank;
        private String botNameTemplate;
        private BotSkin botSkin;
        private BotArmorType armorType;
        private BotArmorType minArmorType;
        private BotArmorType maxArmorType;
        private int totemCount;
        private int minTotemCount;
        private int maxTotemCount;
        private BotRank rank;
        private BotRank minRank;
        private BotRank maxRank;

        private Builder() {
        }

        @Override
        public BotSkinStep setBotNameTemplate(String botNameTemplate) {
            this.botNameTemplate = Objects.requireNonNull(botNameTemplate, "botNameTemplate").trim();
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
        public ChangeableBlastStep blastProtection(int bootsBlastEnabled,
                                                   int leggingsBlastEnabled,
                                                   int chestplateBlastEnabled,
                                                   int helmetBlastEnabled) {
            this.blastProtection = BotBlastProtection.of(
                    bootsBlastEnabled,
                    leggingsBlastEnabled,
                    chestplateBlastEnabled,
                    helmetBlastEnabled
            );
            return this;
        }

        @Override
        public ChangeableBlastStep blastProtection(boolean bootsBlastEnabled,
                                                   boolean leggingsBlastEnabled,
                                                   boolean chestplateBlastEnabled,
                                                   boolean helmetBlastEnabled) {
            this.blastProtection = BotBlastProtection.of(
                    bootsBlastEnabled,
                    leggingsBlastEnabled,
                    chestplateBlastEnabled,
                    helmetBlastEnabled
            );
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
        public BotSettings build() {
            blastProtection = Objects.requireNonNull(blastProtection, "blastProtection");
            armorType = Objects.requireNonNull(armorType, "armorType");
            minArmorType = Objects.requireNonNull(minArmorType, "minArmorType");
            maxArmorType = Objects.requireNonNull(maxArmorType, "maxArmorType");
            rank = Objects.requireNonNull(rank, "rank");
            minRank = Objects.requireNonNull(minRank, "minRank");
            maxRank = Objects.requireNonNull(maxRank, "maxRank");
            botSkin = Objects.requireNonNull(botSkin, "botSkin");
            if (botNameTemplate == null || botNameTemplate.isBlank()) {
                throw new IllegalArgumentException("botNameTemplate cannot be blank");
            }
            validateArmorRange(minArmorType, maxArmorType);
            validateTotemRange(minTotemCount, maxTotemCount);
            validateRankRange(minRank, maxRank);

            if (armorType.ordinal() < minArmorType.ordinal() || armorType.ordinal() > maxArmorType.ordinal()) {
                throw new IllegalArgumentException(
                        "armor must be within range [" + minArmorType + ", " + maxArmorType + "]"
                );
            }

            if (totemCount < minTotemCount || totemCount > maxTotemCount) {
                if (totemCount != -1 || minTotemCount > -1) {
                    throw new IllegalArgumentException(
                            "totemCount must be within range [" + minTotemCount + ", " + maxTotemCount + "]"
                    );
                }
            }

            if (rank.ordinal() < minRank.ordinal() || rank.ordinal() > maxRank.ordinal()) {
                throw new IllegalArgumentException(
                        "rank must be within range [" + minRank + ", " + maxRank + "]"
                );
            }

            if (!follow && combat) {
                throw new IllegalArgumentException("combat cannot be true when follow is false");
            }

            if (!follow && !changeableFollow && changeableCombat) {
                throw new IllegalArgumentException("combat cannot be changeable when follow is fixed to false");
            }

            if (combat && !changeableCombat && changeableFollow) {
                throw new IllegalArgumentException("follow cannot be changeable while combat is fixed to true");
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
            if (minArmorType.ordinal() > maxArmorType.ordinal()) {
                throw new IllegalArgumentException("min armor cannot be greater than max armor");
            }
        }

        private static void validateRankRange(BotRank min, BotRank max) {
            if (min.ordinal() > max.ordinal()) {
                throw new IllegalArgumentException("min rank cannot be greater than max rank");
            }
        }
    }
}
