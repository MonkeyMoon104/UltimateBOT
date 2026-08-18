package com.monkey.ultimatebot.common.model;

import java.util.Objects;

public final class CombatTuning {
    private final double attackRange;
    private final int attackCooldownTicks;
    private final int reactionTicks;
    private final double movementSpeed;
    private final double strafeStrength;
    private final double aimAccuracy;
    private final double aggression;
    private final double retreatHealthRatio;
    private final double healingHealthRatio;
    private final int specialActionCooldownTicks;
    private final double pearlTriggerDistance;
    private final int maxActionsPerTick;
    private final double defensiveChance;
    private final double sprintResetChance;

    public CombatTuning(
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
        this.attackRange = attackRange;
        this.attackCooldownTicks = attackCooldownTicks;
        this.reactionTicks = reactionTicks;
        this.movementSpeed = movementSpeed;
        this.strafeStrength = strafeStrength;
        this.aimAccuracy = aimAccuracy;
        this.aggression = aggression;
        this.retreatHealthRatio = retreatHealthRatio;
        this.healingHealthRatio = healingHealthRatio;
        this.specialActionCooldownTicks = specialActionCooldownTicks;
        this.pearlTriggerDistance = pearlTriggerDistance;
        this.maxActionsPerTick = maxActionsPerTick;
        this.defensiveChance = defensiveChance;
        this.sprintResetChance = sprintResetChance;
    }

    public static Builder builder() {
        return new Builder();
    }

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

    public double attackRange() {
        return attackRange;
    }

    public int attackCooldownTicks() {
        return attackCooldownTicks;
    }

    public int reactionTicks() {
        return reactionTicks;
    }

    public double movementSpeed() {
        return movementSpeed;
    }

    public double strafeStrength() {
        return strafeStrength;
    }

    public double aimAccuracy() {
        return aimAccuracy;
    }

    public double aggression() {
        return aggression;
    }

    public double retreatHealthRatio() {
        return retreatHealthRatio;
    }

    public double healingHealthRatio() {
        return healingHealthRatio;
    }

    public int specialActionCooldownTicks() {
        return specialActionCooldownTicks;
    }

    public double pearlTriggerDistance() {
        return pearlTriggerDistance;
    }

    public int maxActionsPerTick() {
        return maxActionsPerTick;
    }

    public double defensiveChance() {
        return defensiveChance;
    }

    public double sprintResetChance() {
        return sprintResetChance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CombatTuning)) {
            return false;
        }
        CombatTuning other = (CombatTuning) obj;
        return Double.compare(attackRange, other.attackRange) == 0
                && attackCooldownTicks == other.attackCooldownTicks
                && reactionTicks == other.reactionTicks
                && Double.compare(movementSpeed, other.movementSpeed) == 0
                && Double.compare(strafeStrength, other.strafeStrength) == 0
                && Double.compare(aimAccuracy, other.aimAccuracy) == 0
                && Double.compare(aggression, other.aggression) == 0
                && Double.compare(retreatHealthRatio, other.retreatHealthRatio) == 0
                && Double.compare(healingHealthRatio, other.healingHealthRatio) == 0
                && specialActionCooldownTicks == other.specialActionCooldownTicks
                && Double.compare(pearlTriggerDistance, other.pearlTriggerDistance) == 0
                && maxActionsPerTick == other.maxActionsPerTick
                && Double.compare(defensiveChance, other.defensiveChance) == 0
                && Double.compare(sprintResetChance, other.sprintResetChance) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
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

    @Override
    public String toString() {
        return "CombatTuning[attackRange="
                + attackRange
                + ", attackCooldownTicks="
                + attackCooldownTicks
                + ", reactionTicks="
                + reactionTicks
                + ", movementSpeed="
                + movementSpeed
                + ", strafeStrength="
                + strafeStrength
                + ", aimAccuracy="
                + aimAccuracy
                + ", aggression="
                + aggression
                + ", retreatHealthRatio="
                + retreatHealthRatio
                + ", healingHealthRatio="
                + healingHealthRatio
                + ", specialActionCooldownTicks="
                + specialActionCooldownTicks
                + ", pearlTriggerDistance="
                + pearlTriggerDistance
                + ", maxActionsPerTick="
                + maxActionsPerTick
                + ", defensiveChance="
                + defensiveChance
                + ", sprintResetChance="
                + sprintResetChance
                + ']';
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
