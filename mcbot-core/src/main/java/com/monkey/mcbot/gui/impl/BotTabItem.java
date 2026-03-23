package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Material;
import xyz.xenondevs.invui.gui.TabGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.TabItem;

public class BotTabItem extends TabItem {

    private final int tab;
    private final MinecraftBot training;

    public BotTabItem(int tab, MinecraftBot training) {
        super(tab);
        this.tab = tab;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(TabGui gui) {
        boolean selected = gui.getCurrentTab() == tab;

        String basePath = "gui.tab-item.tab-" + tab + "." + (selected ? "selected" : "unselected");

        String materialName = training.getConfig().getString(basePath + ".material",
                selected ? "GLOWSTONE_DUST" : "GUNPOWDER");
        String displayName = training.getConfig().getString(basePath + ".name",
                "&eTab " + tab + (selected ? " &7(selezionato)" : ""));

        Material mat;
        try {
            mat = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            mat = selected ? Material.GLOWSTONE_DUST : Material.GUNPOWDER;
        }

        return new ItemBuilder(mat)
                .setDisplayName(ChatColorUtils.translate(displayName));
    }
}