package com.monkey.ultimatebot.gui.v26_2.tab;

import com.monkey.ultimatebot.gui.combat.CombatTuningProperty;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.CombatModeItem;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.CombatTuningItem;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.DifficultyItem;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.ResetCombatTuningItem;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import com.monkey.ultimatebot.libs.invui.gui.Gui;

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
        return Gui.builder()
                .setStructure(
                        "# m d . . r . #", "# a b c e f g #", "# h i j k l n #", "# o p . . . . #", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('m', combatModeItem)
                .addIngredient('d', new DifficultyItem(context.getTraining(), options, refreshDifficultyDependents))
                .addIngredient('r', new ResetCombatTuningItem(options))
                .addIngredient('a', requireTuningItem(tuningItems, CombatTuningProperty.ATTACK_RANGE))
                .addIngredient('b', requireTuningItem(tuningItems, CombatTuningProperty.ATTACK_COOLDOWN))
                .addIngredient('c', requireTuningItem(tuningItems, CombatTuningProperty.REACTION_TIME))
                .addIngredient('e', requireTuningItem(tuningItems, CombatTuningProperty.MOVEMENT_SPEED))
                .addIngredient('f', requireTuningItem(tuningItems, CombatTuningProperty.STRAFE_STRENGTH))
                .addIngredient('g', requireTuningItem(tuningItems, CombatTuningProperty.AIM_ACCURACY))
                .addIngredient('h', requireTuningItem(tuningItems, CombatTuningProperty.AGGRESSION))
                .addIngredient('i', requireTuningItem(tuningItems, CombatTuningProperty.RETREAT_HEALTH))
                .addIngredient('j', requireTuningItem(tuningItems, CombatTuningProperty.HEALING_HEALTH))
                .addIngredient('k', requireTuningItem(tuningItems, CombatTuningProperty.SPECIAL_COOLDOWN))
                .addIngredient('l', requireTuningItem(tuningItems, CombatTuningProperty.PEARL_DISTANCE))
                .addIngredient('n', requireTuningItem(tuningItems, CombatTuningProperty.ACTIONS_PER_TICK))
                .addIngredient('o', requireTuningItem(tuningItems, CombatTuningProperty.DEFENSIVE_CHANCE))
                .addIngredient('p', requireTuningItem(tuningItems, CombatTuningProperty.SPRINT_RESET_CHANCE))
                .build();
    }

    private CombatTuningItem tuning(CombatTuningProperty property) {
        return new CombatTuningItem(context.getOptions(), property);
    }

    private static CombatTuningItem requireTuningItem(
            Map<CombatTuningProperty, CombatTuningItem> items, CombatTuningProperty property) {
        return Objects.requireNonNull(items.get(property), () -> "Missing tuning item for " + property);
    }
}
