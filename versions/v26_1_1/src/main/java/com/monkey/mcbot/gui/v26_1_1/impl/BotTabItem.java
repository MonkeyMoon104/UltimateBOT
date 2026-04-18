package com.monkey.mcbot.gui.v26_1_1.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.TabGui;
import xyz.xenondevs.invui.item.AbstractTabGuiBoundItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class BotTabItem extends AbstractTabGuiBoundItem {

    private static final int HIDDEN_TAB = 1;
    private static final String HIDDEN_TAB_NAME = "???";

    private final int tab;
    private final MinecraftBot training;

    public BotTabItem(int tab, MinecraftBot training) {
        this.tab = tab;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        TabGui gui = getGui();
        boolean selected = gui.getTab() == tab;

        String basePath = "gui.tab-item.tab-" + tab + "." + (selected ? "selected" : "unselected");

        String materialName = training.getLangString(basePath + ".material",
                selected ? "GLOWSTONE_DUST" : "GUNPOWDER");
        String displayName = isTemporarilyHiddenTab()
                ? HIDDEN_TAB_NAME
                : resolveConfiguredDisplayName(basePath, selected);

        Material mat;
        try {
            mat = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            mat = selected ? Material.GLOWSTONE_DUST : Material.GUNPOWDER;
        }

        return new ItemBuilder(mat)
                .setLegacyName(ChatColorUtils.translate(displayName));
    }

    @Override
    public void handleClick(ClickType clickType, Player player, Click click) {
        if (isTemporarilyHiddenTab()) {
            return;
        }
        getGui().setTab(tab);
    }

    private boolean isTemporarilyHiddenTab() {
        return tab == HIDDEN_TAB;
    }

    private String resolveConfiguredDisplayName(String basePath, boolean selected) {
        return training.getLangString(basePath + ".name",
                "&eTab " + tab + (selected ? " &7(selezionato)" : ""));
    }
}