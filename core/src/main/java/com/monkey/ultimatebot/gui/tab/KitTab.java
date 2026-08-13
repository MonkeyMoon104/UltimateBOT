package com.monkey.ultimatebot.gui.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.gui.impl.action.SpawnItem;
import com.monkey.ultimatebot.gui.impl.action.TeleportItem;
import com.monkey.ultimatebot.gui.impl.settings.CombatItem;
import com.monkey.ultimatebot.gui.impl.settings.CombatModeItem;
import com.monkey.ultimatebot.gui.impl.settings.DifficultyItem;
import com.monkey.ultimatebot.gui.impl.settings.FollowItem;
import com.monkey.ultimatebot.gui.impl.settings.TargetModeItem;
import com.monkey.ultimatebot.gui.impl.settings.TotemItem;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class KitTab {

    private final BotGuiTabContext context;
    private final Runnable refreshModeDependents;

    public KitTab(BotGuiTabContext context, Runnable refreshModeDependents) {
        this.context = Objects.requireNonNull(context, "context");
        this.refreshModeDependents = Objects.requireNonNull(refreshModeDependents, "refreshModeDependents");
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();
        CombatItem combatItem = new CombatItem(context.getTraining(), options);
        FollowItem followItem = new FollowItem(context.getTraining(), options, combatItem);
        CombatModeItem combatModeItem = new CombatModeItem(context.getTraining(), options, refreshModeDependents);

        return Gui.normal()
                .setStructure(
                        "# r . . . . # #", "# . . t . . # #", "# . s g f . # #", "# c . o m . # #", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('r', new DifficultyItem(context.getTraining(), options, combatModeItem::notifyWindows))
                .addIngredient('t', new TotemItem(options, context.getTraining()))
                .addIngredient('f', followItem)
                .addIngredient('s', new SpawnItem(context.getTraining(), context.getViewer(), options))
                .addIngredient(
                        'g',
                        context.hasManagedBotSpawned()
                                ? new TeleportItem(context.getTraining())
                                : new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('c', combatItem)
                .addIngredient('o', combatModeItem)
                .addIngredient('m', new TargetModeItem(context.getTraining(), options))
                .build();
    }
}
