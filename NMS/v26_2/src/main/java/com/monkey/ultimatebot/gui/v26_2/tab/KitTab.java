package com.monkey.ultimatebot.gui.v26_2.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.gui.v26_2.impl.action.SpawnItem;
import com.monkey.ultimatebot.gui.v26_2.impl.action.TeleportItem;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.CombatItem;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.CombatModeItem;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.DifficultyItem;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.FollowItem;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.TargetModeItem;
import com.monkey.ultimatebot.gui.v26_2.impl.settings.TotemItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;

public class KitTab {

    private final BotGuiTabContext context;

    public KitTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();
        CombatItem combatItem = new CombatItem(context.getTraining(), options);
        FollowItem followItem = new FollowItem(context.getTraining(), options, combatItem);

        Gui gui = Gui.builder()
                .setStructure(
                        "# r . . . . # #", "# . . t . . # #", "# . s g f . # #", "# c . o m . # #", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', Item.simple(new ItemStack(Material.AIR)))
                .addIngredient('r', new DifficultyItem(context.getTraining(), options))
                .addIngredient('t', new TotemItem(options, context.getTraining()))
                .addIngredient('f', followItem)
                .addIngredient('s', new SpawnItem(context.getTraining(), context.getViewer(), options))
                .addIngredient(
                        'g',
                        context.hasManagedBotSpawned()
                                ? new TeleportItem(context.getTraining())
                                : Item.simple(new ItemStack(Material.AIR)))
                .addIngredient('c', combatItem)
                .addIngredient('o', new CombatModeItem(context.getTraining(), options))
                .addIngredient('m', new TargetModeItem(context.getTraining(), options))
                .build();

        return gui;
    }
}
