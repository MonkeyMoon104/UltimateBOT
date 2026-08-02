package com.monkey.ultimatebot.gui.v26_2.tab;

import com.monkey.ultimatebot.gui.combat.CombatTuningProperty;
import com.monkey.ultimatebot.gui.v26_2.impl.CombatModeItem;
import com.monkey.ultimatebot.gui.v26_2.impl.CombatTuningItem;
import com.monkey.ultimatebot.gui.v26_2.impl.DifficultyItem;
import com.monkey.ultimatebot.gui.v26_2.impl.ResetCombatTuningItem;
import java.util.Objects;
import org.bukkit.Material;
import xyz.xenondevs.invui.gui.Gui;

public final class CombatSettingsTab {
    private final BotGuiTabContext context;

    public CombatSettingsTab(BotGuiTabContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public Gui build(Material borderMaterial, String borderName) {
        var options = context.getOptions();
        return Gui.builder()
                .setStructure(
                        "# m d . . r . #", "# a b c e f g #", "# h i j k l n #", "# o p . . . . #", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('m', new CombatModeItem(context.getTraining(), options))
                .addIngredient('d', new DifficultyItem(context.getTraining(), options))
                .addIngredient('r', new ResetCombatTuningItem(options))
                .addIngredient('a', tuning(CombatTuningProperty.ATTACK_RANGE))
                .addIngredient('b', tuning(CombatTuningProperty.ATTACK_COOLDOWN))
                .addIngredient('c', tuning(CombatTuningProperty.REACTION_TIME))
                .addIngredient('e', tuning(CombatTuningProperty.MOVEMENT_SPEED))
                .addIngredient('f', tuning(CombatTuningProperty.STRAFE_STRENGTH))
                .addIngredient('g', tuning(CombatTuningProperty.AIM_ACCURACY))
                .addIngredient('h', tuning(CombatTuningProperty.AGGRESSION))
                .addIngredient('i', tuning(CombatTuningProperty.RETREAT_HEALTH))
                .addIngredient('j', tuning(CombatTuningProperty.HEALING_HEALTH))
                .addIngredient('k', tuning(CombatTuningProperty.SPECIAL_COOLDOWN))
                .addIngredient('l', tuning(CombatTuningProperty.PEARL_DISTANCE))
                .addIngredient('n', tuning(CombatTuningProperty.ACTIONS_PER_TICK))
                .addIngredient('o', tuning(CombatTuningProperty.DEFENSIVE_CHANCE))
                .addIngredient('p', tuning(CombatTuningProperty.SPRINT_RESET_CHANCE))
                .build();
    }

    private CombatTuningItem tuning(CombatTuningProperty property) {
        return new CombatTuningItem(context.getOptions(), property);
    }
}
