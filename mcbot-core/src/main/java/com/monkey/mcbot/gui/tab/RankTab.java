package com.monkey.mcbot.gui.tab;

import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.gui.impl.RankItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class RankTab {

    private final BotGuiTabContext context;

    public RankTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();

        return Gui.normal()
                .setStructure(
                        "# # # # # # # #", "# . . r . . . #", "# . . . . . . #", "# . . . . . . #", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('r', new RankItem(context.getTraining(), options))
                .build();
    }
}
