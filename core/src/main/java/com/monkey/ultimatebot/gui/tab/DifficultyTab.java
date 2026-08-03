package com.monkey.ultimatebot.gui.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.gui.impl.settings.DifficultyItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class DifficultyTab {

    private final BotGuiTabContext context;

    public DifficultyTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();

        return Gui.normal()
                .setStructure(
                        "# # # # # # # #", "# . . r . . . #", "# . . . . . . #", "# . . . . . . #", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('r', new DifficultyItem(context.getTraining(), options))
                .build();
    }
}
