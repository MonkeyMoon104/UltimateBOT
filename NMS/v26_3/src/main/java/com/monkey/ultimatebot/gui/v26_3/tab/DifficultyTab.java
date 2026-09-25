package com.monkey.ultimatebot.gui.v26_3.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.gui.v26_3.impl.settings.DifficultyItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.monkey.ultimatebot.libs.invui.gui.Gui;
import com.monkey.ultimatebot.libs.invui.item.Item;

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
