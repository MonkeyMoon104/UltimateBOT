package com.monkey.ultimatebot.gui.v26_2.impl;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.TabGui;
import xyz.xenondevs.invui.item.AbstractTabGuiBoundItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class BotTabItem extends AbstractTabGuiBoundItem {

    private final int tab;
    private final UltimateBot training;

    public BotTabItem(int tab, UltimateBot training) {
        this.tab = tab;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        TabGui gui = getGui();
        boolean selected = gui.getTab() == tab;

        String basePath = "gui.tab-item.tab-" + tab + "." + (selected ? "selected" : "unselected");

        String materialName = training.getLangString(basePath + ".material", selected ? "GLOWSTONE_DUST" : "GUNPOWDER");
        String displayName = resolveConfiguredDisplayName(basePath, selected);

        Material mat;
        try {
            mat = Material.valueOf(materialName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            mat = selected ? Material.GLOWSTONE_DUST : Material.GUNPOWDER;
        }

        return new ItemBuilder(mat).setLegacyName(ChatColorUtils.translate(displayName));
    }

    @Override
    public void handleClick(ClickType clickType, Player player, Click click) {
        getGui().setTab(tab);
    }

    private String resolveConfiguredDisplayName(String basePath, boolean selected) {
        return training.getLangString(basePath + ".name", "&eTab " + tab + (selected ? " &7(selezionato)" : ""));
    }
}
