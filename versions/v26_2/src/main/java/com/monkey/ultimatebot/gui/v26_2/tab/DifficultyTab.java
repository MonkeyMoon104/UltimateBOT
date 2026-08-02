package com.monkey.ultimatebot.gui.v26_2.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.gui.v26_2.impl.DifficultyItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;

public class DifficultyTab {

    private final BotGuiTabContext context;

    public DifficultyTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();

        return Gui.builder()
                .setStructure(
                        "# # # # # # # #", "# . . r . . . #", "# . . . . . . #", "# . . . . . . #", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', Item.simple(new ItemStack(Material.AIR)))
                .addIngredient('r', new DifficultyItem(context.getTraining(), options))
                .build();
    }
}
