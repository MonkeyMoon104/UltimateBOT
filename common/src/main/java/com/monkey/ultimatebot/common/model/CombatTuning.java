package com.monkey.ultimatebot.common.model;

/** Immutable performance and behavior tuning shared by all combat modes. */
public record CombatTuning(
        double attackRange,
        int attackCooldownTicks,
        int reactionTicks,
        double movementSpeed,
        double strafeStrength,
        double aimAccuracy,
        double aggression,
        double retreatHealthRatio,
        double healingHealthRatio,
        int specialActionCooldownTicks,
        double pearlTriggerDistance,
        int maxActionsPerTick,
        double defensiveChance,
        double sprintResetChance) {

    public CombatTuning {
        requireRange(attackRange, 1.0D, 6.0D, "attackRange");
        requireRange(attackCooldownTicks, 0, 40, "attackCooldownTicks");
        requireRange(reactionTicks, 0, 100, "reactionTicks");
        requireRange(movementSpeed, 0.05D, 1.0D, "movementSpeed");
        requireRatio(strafeStrength, "strafeStrength");
        requireRatio(aimAccuracy, "aimAccuracy");
        requireRatio(aggression, "aggression");
        requireRatio(retreatHealthRatio, "retreatHealthRatio");
        requireRatio(healingHealthRatio, "healingHealthRatio");
        requireRange(specialActionCooldownTicks, 0, 400, "specialActionCooldownTicks");
        requireRange(pearlTriggerDistance, 0.0D, 64.0D, "pearlTriggerDistance");
        requireRange(maxActionsPerTick, 1, 8, "maxActionsPerTick");
        requireRatio(defensiveChance, "defensiveChance");
        requireRatio(sprintResetChance, "sprintResetChance");
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Creates a builder initialized with every value from this tuning. */
    public Builder toBuilder() {
        return builder()
                .attackRange(attackRange)
                .attackCooldownTicks(attackCooldownTicks)
                .reactionTicks(reactionTicks)
                .movementSpeed(movementSpeed)
                .strafeStrength(strafeStrength)
                .aimAccuracy(aimAccuracy)
                .aggression(aggression)
                .retreatHealthRatio(retreatHealthRatio)
                .healingHealthRatio(healingHealthRatio)
                .specialActionCooldownTicks(specialActionCooldownTicks)
                .pearlTriggerDistance(pearlTriggerDistance)
                .maxActionsPerTick(maxActionsPerTick)
                .defensiveChance(defensiveChance)
                .sprintResetChance(sprintResetChance);
    }

    private static void requireRatio(double value, String name) {
        requireRange(value, 0.0D, 1.0D, name);
    }

    private static void requireRange(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
    }

    private static void requireRange(int value, int minimum, int maximum, String name) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
    }

    public static final class Builder {
        private double attackRange = 3.2D;
        private int attackCooldownTicks = 10;
        private int reactionTicks = 6;
        private double movementSpeed = 0.32D;
        private double strafeStrength = 0.5D;
        private double aimAccuracy = 0.82D;
        private double aggression = 0.55D;
        private double retreatHealthRatio = 0.18D;
        private double healingHealthRatio = 0.55D;
        private int specialActionCooldownTicks = 20;
        private double pearlTriggerDistance = 12.0D;
        private int maxActionsPerTick = 2;
        private double defensiveChance = 0.35D;
        private double sprintResetChance = 0.5D;

        private Builder() {}

        public Builder attackRange(double value) {
            attackRange = value;
            return this;
        }

        public Builder attackCooldownTicks(int value) {
            attackCooldownTicks = value;
            return this;
        }

        public Builder reactionTicks(int value) {
            reactionTicks = value;
            return this;
        }

        public Builder movementSpeed(double value) {
            movementSpeed = value;
            return this;
        }

        public Builder strafeStrength(double value) {
            strafeStrength = value;
            return this;
        }

        public Builder aimAccuracy(double value) {
            aimAccuracy = value;
            return this;
        }

        public Builder aggression(double value) {
            aggression = value;
            return this;
        }

        public Builder retreatHealthRatio(double value) {
            retreatHealthRatio = value;
            return this;
        }

        public Builder healingHealthRatio(double value) {
            healingHealthRatio = value;
            return this;
        }

        public Builder specialActionCooldownTicks(int value) {
            specialActionCooldownTicks = value;
            return this;
        }

        public Builder pearlTriggerDistance(double value) {
            pearlTriggerDistance = value;
            return this;
        }

        public Builder maxActionsPerTick(int value) {
            maxActionsPerTick = value;
            return this;
        }

        public Builder defensiveChance(double value) {
            defensiveChance = value;
            return this;
        }

        public Builder sprintResetChance(double value) {
            sprintResetChance = value;
            return this;
        }

        public CombatTuning build() {
            return new CombatTuning(
                    attackRange,
                    attackCooldownTicks,
                    reactionTicks,
                    movementSpeed,
                    strafeStrength,
                    aimAccuracy,
                    aggression,
                    retreatHealthRatio,
                    healingHealthRatio,
                    specialActionCooldownTicks,
                    pearlTriggerDistance,
                    maxActionsPerTick,
                    defensiveChance,
                    sprintResetChance);
        }
    }
}
