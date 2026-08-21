package com.monkey.ultimatebot.gui.combat;

import com.monkey.ultimatebot.common.model.combat.CombatTuning;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.Material;

public enum CombatTuningProperty {
    ATTACK_RANGE("Attack range", "IRON_SWORD", 0.1D, 1.0D, 6.0D, false),
    ATTACK_COOLDOWN("Attack cooldown", "CLOCK", 1.0D, 0.0D, 40.0D, true),
    REACTION_TIME("Reaction time", "REPEATER", 1.0D, 0.0D, 100.0D, true),
    MOVEMENT_SPEED("Movement speed", "SUGAR", 0.01D, 0.05D, 1.0D, false),
    STRAFE_STRENGTH("Strafe strength", "FEATHER", 0.05D, 0.0D, 1.0D, false),
    AIM_ACCURACY("Aim accuracy", "ARROW", 0.05D, 0.0D, 1.0D, false),
    AGGRESSION("Aggression", "BLAZE_POWDER", 0.05D, 0.0D, 1.0D, false),
    RETREAT_HEALTH("Retreat health", "REDSTONE", 0.05D, 0.0D, 1.0D, false),
    HEALING_HEALTH("Healing health", "GOLDEN_APPLE", 0.05D, 0.0D, 1.0D, false),
    SPECIAL_COOLDOWN("Special cooldown", "COMPASS", 1.0D, 0.0D, 400.0D, true),
    PEARL_DISTANCE("Pearl distance", "ENDER_PEARL", 0.5D, 0.0D, 64.0D, false),
    ACTIONS_PER_TICK("Actions per tick", "COMPARATOR", 1.0D, 1.0D, 8.0D, true),
    DEFENSIVE_CHANCE("Defensive chance", "SHIELD", 0.05D, 0.0D, 1.0D, false),
    SPRINT_RESET_CHANCE("Sprint reset chance", "DIAMOND_BOOTS", 0.05D, 0.0D, 1.0D, false);

    private final String displayName;
    private final String materialName;
    private final double step;
    private final double minimum;
    private final double maximum;
    private final boolean integer;

    CombatTuningProperty(
            String displayName, String materialName, double step, double minimum, double maximum, boolean integer) {
        this.displayName = displayName;
        this.materialName = materialName;
        this.step = step;
        this.minimum = minimum;
        this.maximum = maximum;
        this.integer = integer;
    }

    public String displayName() {
        return displayName;
    }

    public Material material() {
        if (this == SPECIAL_COOLDOWN) {
            return MaterialCatalog.optional("RECOVERY_COMPASS", Material.COMPASS);
        }
        if (this == AIM_ACCURACY) {
            return MaterialCatalog.optional("TARGET", Material.ARROW);
        }

        return MaterialCatalog.optional(materialName, Material.STONE);
    }

    public String formattedValue(CombatTuning tuning) {
        double value = value(Objects.requireNonNull(tuning, "tuning"));
        if (integer) {
            return Integer.toString((int) Math.round(value));
        }
        if (this == AIM_ACCURACY
                || this == AGGRESSION
                || this == RETREAT_HEALTH
                || this == HEALING_HEALTH
                || this == DEFENSIVE_CHANCE
                || this == SPRINT_RESET_CHANCE
                || this == STRAFE_STRENGTH) {
            return Math.round(value * 100.0D) + "%";
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    public CombatTuning adjust(CombatTuning tuning, boolean increase, boolean accelerated) {
        Objects.requireNonNull(tuning, "tuning");
        double direction = increase ? 1.0D : -1.0D;
        double multiplier = accelerated ? 5.0D : 1.0D;
        double adjusted = clamp(value(tuning) + direction * step * multiplier);
        CombatTuning.Builder builder = tuning.toBuilder();
        write(builder, adjusted);
        return builder.build();
    }

    private double value(CombatTuning tuning) {
        switch (this) {
            case ATTACK_RANGE:
                return tuning.attackRange();
            case ATTACK_COOLDOWN:
                return tuning.attackCooldownTicks();
            case REACTION_TIME:
                return tuning.reactionTicks();
            case MOVEMENT_SPEED:
                return tuning.movementSpeed();
            case STRAFE_STRENGTH:
                return tuning.strafeStrength();
            case AIM_ACCURACY:
                return tuning.aimAccuracy();
            case AGGRESSION:
                return tuning.aggression();
            case RETREAT_HEALTH:
                return tuning.retreatHealthRatio();
            case HEALING_HEALTH:
                return tuning.healingHealthRatio();
            case SPECIAL_COOLDOWN:
                return tuning.specialActionCooldownTicks();
            case PEARL_DISTANCE:
                return tuning.pearlTriggerDistance();
            case ACTIONS_PER_TICK:
                return tuning.maxActionsPerTick();
            case DEFENSIVE_CHANCE:
                return tuning.defensiveChance();
            case SPRINT_RESET_CHANCE:
                return tuning.sprintResetChance();
        }
        throw new IllegalStateException("Unexpected switch value");
    }

    private void write(CombatTuning.Builder builder, double value) {
        switch (this) {
            case ATTACK_RANGE:
                builder.attackRange(value);
                break;
            case ATTACK_COOLDOWN:
                builder.attackCooldownTicks((int) Math.round(value));
                break;
            case REACTION_TIME:
                builder.reactionTicks((int) Math.round(value));
                break;
            case MOVEMENT_SPEED:
                builder.movementSpeed(value);
                break;
            case STRAFE_STRENGTH:
                builder.strafeStrength(value);
                break;
            case AIM_ACCURACY:
                builder.aimAccuracy(value);
                break;
            case AGGRESSION:
                builder.aggression(value);
                break;
            case RETREAT_HEALTH:
                builder.retreatHealthRatio(value);
                break;
            case HEALING_HEALTH:
                builder.healingHealthRatio(value);
                break;
            case SPECIAL_COOLDOWN:
                builder.specialActionCooldownTicks((int) Math.round(value));
                break;
            case PEARL_DISTANCE:
                builder.pearlTriggerDistance(value);
                break;
            case ACTIONS_PER_TICK:
                builder.maxActionsPerTick((int) Math.round(value));
                break;
            case DEFENSIVE_CHANCE:
                builder.defensiveChance(value);
                break;
            case SPRINT_RESET_CHANCE:
                builder.sprintResetChance(value);
                break;
        }
    }

    private double clamp(double value) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
