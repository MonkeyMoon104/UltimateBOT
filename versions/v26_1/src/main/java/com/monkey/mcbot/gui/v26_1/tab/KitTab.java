package com.monkey.mcbot.gui.v26_1.tab;

import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.gui.v26_1.impl.*;
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
                        "# r . . . . # #",
                        "# . . t . . # #",
                        "# . s g f . # #",
                        "# c . . m . # #",
                        "# # # # # # # #"
                )
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', Item.simple(new ItemStack(Material.AIR)))
                .addIngredient('r', new RankItem(context.getTraining(), options))
                .addIngredient('t', new TotemItem(options, context.getTraining()))
                .addIngredient('f', followItem)
                .addIngredient('s', new SpawnItem(context.getTraining(), context.getViewer(), options))
                .addIngredient('g', context.hasManagedBotSpawned()
                        ? new TeleportItem(context.getTraining())
                        : Item.simple(new ItemStack(Material.AIR)))
                .addIngredient('c', combatItem)
                .addIngredient('m', new TargetModeItem(context.getTraining(), options))
                .build();

        return gui;
    }
}
