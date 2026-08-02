package com.monkey.ultimatebot.gui.v26_1.impl;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class TrimBackgroundItem extends AbstractItem {

    private final UltimateBot training;

    public TrimBackgroundItem(UltimateBot training) {
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        Material material = Material.matchMaterial(
                training.getLangString("gui.templates-background.material", "LIGHT_GRAY_STAINED_GLASS_PANE"));
        if (material == null) {
            material = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
        }

        ItemBuilder builder = new ItemBuilder(material);
        builder.setLegacyName(
                ChatColorUtils.translate(training.getLangString("gui.templates-background.name", "&7Background")));

        for (String line : training.getLangStringList("gui.templates-background.lore")) {
            builder.addLegacyLoreLines(ChatColorUtils.translate(line));
        }

        return builder;
    }

    @Override
    public void handleClick(ClickType clickType, Player player, Click click) {}
}
