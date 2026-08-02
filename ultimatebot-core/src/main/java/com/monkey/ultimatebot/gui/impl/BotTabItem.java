package com.monkey.ultimatebot.gui.impl;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.Locale;
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

    private final int tab;
    private final UltimateBot training;

    public BotTabItem(int tab, UltimateBot training) {
        super(tab);
        this.tab = tab;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(TabGui gui) {
        boolean selected = gui.getCurrentTab() == tab;

        String basePath = "gui.tab-item.tab-" + tab + "." + (selected ? "selected" : "unselected");

        String materialName = training.getLangString(basePath + ".material", selected ? "GLOWSTONE_DUST" : "GUNPOWDER");
        String displayName = resolveConfiguredDisplayName(basePath, selected);

        Material mat;
        try {
            mat = Material.valueOf(materialName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            mat = selected ? Material.GLOWSTONE_DUST : Material.GUNPOWDER;
        }

        return new ItemBuilder(mat).setDisplayName(ChatColorUtils.translate(displayName));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        super.handleClick(clickType, player, event);
    }

    private String resolveConfiguredDisplayName(String basePath, boolean selected) {
        return training.getLangString(basePath + ".name", "&eTab " + tab + (selected ? " &7(selezionato)" : ""));
    }
}
