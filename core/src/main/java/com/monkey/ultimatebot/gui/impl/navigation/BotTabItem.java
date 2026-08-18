package com.monkey.ultimatebot.gui.impl.navigation;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.gui.NewBotGUI;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
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
    private final int langTab;
    private final UltimateBot training;

    public BotTabItem(int tab, UltimateBot training) {
        this(tab, tab, training);
    }

    public BotTabItem(int invuiTab, int langTab, UltimateBot training) {
        super(invuiTab);
        this.tab = invuiTab;
        this.langTab = langTab;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(TabGui gui) {
        boolean selected = gui.getCurrentTab() == tab;

        String basePath = "gui.tab-item.tab-" + langTab + "." + (selected ? "selected" : "unselected");

        String materialName = training.getLangString(basePath + ".material", selected ? "GLOWSTONE_DUST" : "GUNPOWDER");
        String displayName = resolveConfiguredDisplayName(basePath, selected);

        Material mat = resolveTabMaterial(materialName, selected);

        return new ItemBuilder(mat).setDisplayName(ChatColorUtils.translate(displayName));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        super.handleClick(clickType, player, event);
    }

    private Material resolveTabMaterial(String materialName, boolean selected) {
        Material fallback = selected ? Material.GLOWSTONE_DUST : Material.GUNPOWDER;

        if (langTab == NewBotGUI.LANG_TAB_TEMPLATES && !MaterialCatalog.feature(PlatformCapability.ARMOR_TRIM)) {
            return MaterialCatalog.optional("IRON_CHESTPLATE", Material.IRON_CHESTPLATE);
        }
        return MaterialCatalog.optional(materialName, fallback);
    }

    private String resolveConfiguredDisplayName(String basePath, boolean selected) {
        return training.getLangString(basePath + ".name", "&eTab " + langTab + (selected ? " &7(selezionato)" : ""));
    }
}
