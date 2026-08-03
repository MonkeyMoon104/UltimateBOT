package com.monkey.ultimatebot.gui.tab;

import com.monkey.ultimatebot.gui.combat.CombatTuningProperty;
import com.monkey.ultimatebot.gui.impl.CombatModeItem;
import com.monkey.ultimatebot.gui.impl.CombatTuningItem;
import com.monkey.ultimatebot.gui.impl.DifficultyItem;
import com.monkey.ultimatebot.gui.impl.ResetCombatTuningItem;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import xyz.xenondevs.invui.gui.Gui;

public final class CombatSettingsTab {
    private final BotGuiTabContext context;
    private final Runnable refreshModeDependents;

    public CombatSettingsTab(BotGuiTabContext context, Runnable refreshModeDependents) {
        this.context = Objects.requireNonNull(context, "context");
        this.refreshModeDependents = Objects.requireNonNull(refreshModeDependents, "refreshModeDependents");
    }

    public Gui build(Material borderMaterial, String borderName) {
        var options = context.getOptions();
        CombatModeItem combatModeItem = new CombatModeItem(context.getTraining(), options, refreshModeDependents);
        Map<CombatTuningProperty, CombatTuningItem> tuningItems = new EnumMap<>(CombatTuningProperty.class);
        for (CombatTuningProperty property : CombatTuningProperty.values()) {
            tuningItems.put(property, tuning(property));
        }
        Runnable refreshDifficultyDependents = () -> {
            combatModeItem.notifyWindows();
            tuningItems.values().forEach(CombatTuningItem::notifyWindows);
        };
        return Gui.normal()
                .setStructure(
                        "# m d . . r . #", "# a b c e f g #", "# h i j k l n #", "# o p . . . . #", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('m', combatModeItem)
                .addIngredient('d', new DifficultyItem(context.getTraining(), options, refreshDifficultyDependents))
                .addIngredient('r', new ResetCombatTuningItem(options))
                .addIngredient('a', tuningItems.get(CombatTuningProperty.ATTACK_RANGE))
                .addIngredient('b', tuningItems.get(CombatTuningProperty.ATTACK_COOLDOWN))
                .addIngredient('c', tuningItems.get(CombatTuningProperty.REACTION_TIME))
                .addIngredient('e', tuningItems.get(CombatTuningProperty.MOVEMENT_SPEED))
                .addIngredient('f', tuningItems.get(CombatTuningProperty.STRAFE_STRENGTH))
                .addIngredient('g', tuningItems.get(CombatTuningProperty.AIM_ACCURACY))
                .addIngredient('h', tuningItems.get(CombatTuningProperty.AGGRESSION))
                .addIngredient('i', tuningItems.get(CombatTuningProperty.RETREAT_HEALTH))
                .addIngredient('j', tuningItems.get(CombatTuningProperty.HEALING_HEALTH))
                .addIngredient('k', tuningItems.get(CombatTuningProperty.SPECIAL_COOLDOWN))
                .addIngredient('l', tuningItems.get(CombatTuningProperty.PEARL_DISTANCE))
                .addIngredient('n', tuningItems.get(CombatTuningProperty.ACTIONS_PER_TICK))
                .addIngredient('o', tuningItems.get(CombatTuningProperty.DEFENSIVE_CHANCE))
                .addIngredient('p', tuningItems.get(CombatTuningProperty.SPRINT_RESET_CHANCE))
                .build();
    }

    private CombatTuningItem tuning(CombatTuningProperty property) {
        return new CombatTuningItem(context.getTraining(), context.getOptions(), property);
    }
}
