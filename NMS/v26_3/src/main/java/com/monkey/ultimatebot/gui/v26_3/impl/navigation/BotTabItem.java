package com.monkey.ultimatebot.gui.v26_3.impl.navigation;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import com.monkey.ultimatebot.libs.invui.Click;
import com.monkey.ultimatebot.libs.invui.gui.TabGui;
import com.monkey.ultimatebot.libs.invui.item.AbstractTabGuiBoundItem;
import com.monkey.ultimatebot.libs.invui.item.ItemBuilder;
import com.monkey.ultimatebot.libs.invui.item.ItemProvider;

public class BotTabItem extends AbstractTabGuiBoundItem {

    private final int tab;
    private final int langTab;
    private final UltimateBot training;

    public BotTabItem(int tab, UltimateBot training) {
        this(tab, tab, training);
    }

    public BotTabItem(int invuiTab, int langTab, UltimateBot training) {
        this.tab = invuiTab;
        this.langTab = langTab;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        TabGui gui = getGui();
        boolean selected = gui.getTab() == tab;

        String basePath = "gui.tab-item.tab-" + langTab + "." + (selected ? "selected" : "unselected");

        String materialName = training.getLangString(basePath + ".material", selected ? "GLOWSTONE_DUST" : "GUNPOWDER");
        String displayName = resolveConfiguredDisplayName(basePath, selected);

        Material mat = MaterialCatalog.optional(materialName, selected ? Material.GLOWSTONE_DUST : Material.GUNPOWDER);

        return new ItemBuilder(mat).setLegacyName(ChatColorUtils.translate(displayName));
    }

    @Override
    public void handleClick(ClickType clickType, Player player, Click click) {
        getGui().setTab(tab);
    }

    private String resolveConfiguredDisplayName(String basePath, boolean selected) {
        return training.getLangString(basePath + ".name", "&eTab " + langTab + (selected ? " &7(selezionato)" : ""));
    }
}
