package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.TabGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.TabItem;

public class BotTabItem extends TabItem {

    private static final int HIDDEN_TAB = 1;
    private static final String HIDDEN_TAB_NAME = "???";

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
                .setDisplayName(ChatColorUtils.translate(displayName));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (isTemporarilyHiddenTab()) {
            return;
        }

        super.handleClick(clickType, player, event);
    }

    private boolean isTemporarilyHiddenTab() {
        return tab == HIDDEN_TAB;
    }

    private String resolveConfiguredDisplayName(String basePath, boolean selected) {
        return training.getConfig().getString(basePath + ".name",
                "&eTab " + tab + (selected ? " &7(selezionato)" : ""));
    }
}
