package com.monkey.ultimatebot.gui.impl.customization;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class TrimBackgroundItem extends AbstractItem {

    private final UltimateBot training;

    public TrimBackgroundItem(UltimateBot training) {
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider() {
        Material material = MaterialCatalog.optional(
                training.getLangString("gui.templates-background.material", "LIGHT_GRAY_STAINED_GLASS_PANE"),
                Material.LIGHT_GRAY_STAINED_GLASS_PANE);

        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(
                ChatColorUtils.translate(training.getLangString("gui.templates-background.name", "&7Background")));

        for (String line : training.getLangStringList("gui.templates-background.lore")) {
            builder.addLoreLines(ChatColorUtils.translate(line));
        }

        return builder;
    }

    @Override
    public void handleClick(
            org.bukkit.event.inventory.ClickType clickType,
            Player player,
            org.bukkit.event.inventory.InventoryClickEvent event) {}
}
