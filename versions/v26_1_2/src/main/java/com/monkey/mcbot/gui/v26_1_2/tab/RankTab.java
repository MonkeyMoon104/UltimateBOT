package com.monkey.mcbot.gui.v26_1_2.tab;

import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.gui.v26_1_2.impl.RankItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;

public class RankTab {

    private final BotGuiTabContext context;

    public RankTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();

        return Gui.builder()
                .setStructure(
                        "# # # # # # # #",
                        "# . . r . . . #",
                        "# . . . . . . #",
                        "# . . . . . . #",
                        "# # # # # # # #"
                )
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', Item.simple(new ItemStack(Material.AIR)))
                .addIngredient('r', new RankItem(context.getTraining(), options))
                .build();
    }
}