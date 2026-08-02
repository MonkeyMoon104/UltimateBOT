package com.monkey.ultimatebot.gui.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.gui.impl.*;
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

        Gui gui = Gui.normal()
                .setStructure(
                        "# r . . . . # #", "# . . t . . # #", "# . s g f . # #", "# c . o m . # #", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('r', new DifficultyItem(context.getTraining(), options))
                .addIngredient('t', new TotemItem(options, context.getTraining()))
                .addIngredient('f', followItem)
                .addIngredient('s', new SpawnItem(context.getTraining(), context.getViewer(), options))
                .addIngredient(
                        'g',
                        context.hasManagedBotSpawned()
                                ? new TeleportItem(context.getTraining())
                                : new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('c', combatItem)
                .addIngredient('o', new CombatModeItem(context.getTraining(), options, refreshModeDependents))
                .addIngredient('m', new TargetModeItem(context.getTraining(), options))
                .build();

        return gui;
    }
}
