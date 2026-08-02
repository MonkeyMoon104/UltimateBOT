package com.monkey.ultimatebot.gui.combat;

import com.monkey.ultimatebot.common.model.CombatTuning;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.Material;

public enum CombatTuningProperty {
    ATTACK_RANGE("Attack range", Material.IRON_SWORD, 0.1D, 1.0D, 6.0D, false),
    ATTACK_COOLDOWN("Attack cooldown", Material.CLOCK, 1.0D, 0.0D, 40.0D, true),
    REACTION_TIME("Reaction time", Material.REPEATER, 1.0D, 0.0D, 100.0D, true),
    MOVEMENT_SPEED("Movement speed", Material.SUGAR, 0.01D, 0.05D, 1.0D, false),
    STRAFE_STRENGTH("Strafe strength", Material.FEATHER, 0.05D, 0.0D, 1.0D, false),
    AIM_ACCURACY("Aim accuracy", Material.TARGET, 0.05D, 0.0D, 1.0D, false),
    AGGRESSION("Aggression", Material.BLAZE_POWDER, 0.05D, 0.0D, 1.0D, false),
    RETREAT_HEALTH("Retreat health", Material.REDSTONE, 0.05D, 0.0D, 1.0D, false),
    HEALING_HEALTH("Healing health", Material.GOLDEN_APPLE, 0.05D, 0.0D, 1.0D, false),
    SPECIAL_COOLDOWN("Special cooldown", Material.RECOVERY_COMPASS, 1.0D, 0.0D, 400.0D, true),
    PEARL_DISTANCE("Pearl distance", Material.ENDER_PEARL, 0.5D, 0.0D, 64.0D, false),
    ACTIONS_PER_TICK("Actions per tick", Material.COMPARATOR, 1.0D, 1.0D, 8.0D, true),
    DEFENSIVE_CHANCE("Defensive chance", Material.SHIELD, 0.05D, 0.0D, 1.0D, false),
    SPRINT_RESET_CHANCE("Sprint reset chance", Material.DIAMOND_BOOTS, 0.05D, 0.0D, 1.0D, false);

    private final String displayName;
    private final Material material;
    private final double step;
    private final double minimum;
    private final double maximum;
    private final boolean integer;

    CombatTuningProperty(
            String displayName, Material material, double step, double minimum, double maximum, boolean integer) {
        this.displayName = displayName;
        this.material = material;
        this.step = step;
        this.minimum = minimum;
        this.maximum = maximum;
        this.integer = integer;
    }

    public String displayName() {
        return displayName;
    }

    public Material material() {
        return material;
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
        return switch (this) {
            case ATTACK_RANGE -> tuning.attackRange();
            case ATTACK_COOLDOWN -> tuning.attackCooldownTicks();
            case REACTION_TIME -> tuning.reactionTicks();
            case MOVEMENT_SPEED -> tuning.movementSpeed();
            case STRAFE_STRENGTH -> tuning.strafeStrength();
            case AIM_ACCURACY -> tuning.aimAccuracy();
            case AGGRESSION -> tuning.aggression();
            case RETREAT_HEALTH -> tuning.retreatHealthRatio();
            case HEALING_HEALTH -> tuning.healingHealthRatio();
            case SPECIAL_COOLDOWN -> tuning.specialActionCooldownTicks();
            case PEARL_DISTANCE -> tuning.pearlTriggerDistance();
            case ACTIONS_PER_TICK -> tuning.maxActionsPerTick();
            case DEFENSIVE_CHANCE -> tuning.defensiveChance();
            case SPRINT_RESET_CHANCE -> tuning.sprintResetChance();
        };
    }

    private void write(CombatTuning.Builder builder, double value) {
        switch (this) {
            case ATTACK_RANGE -> builder.attackRange(value);
            case ATTACK_COOLDOWN -> builder.attackCooldownTicks((int) Math.round(value));
            case REACTION_TIME -> builder.reactionTicks((int) Math.round(value));
            case MOVEMENT_SPEED -> builder.movementSpeed(value);
            case STRAFE_STRENGTH -> builder.strafeStrength(value);
            case AIM_ACCURACY -> builder.aimAccuracy(value);
            case AGGRESSION -> builder.aggression(value);
            case RETREAT_HEALTH -> builder.retreatHealthRatio(value);
            case HEALING_HEALTH -> builder.healingHealthRatio(value);
            case SPECIAL_COOLDOWN -> builder.specialActionCooldownTicks((int) Math.round(value));
            case PEARL_DISTANCE -> builder.pearlTriggerDistance(value);
            case ACTIONS_PER_TICK -> builder.maxActionsPerTick((int) Math.round(value));
            case DEFENSIVE_CHANCE -> builder.defensiveChance(value);
            case SPRINT_RESET_CHANCE -> builder.sprintResetChance(value);
        }
    }

    private double clamp(double value) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
