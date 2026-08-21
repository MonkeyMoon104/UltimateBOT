package com.monkey.ultimatebot.gui.v26_1.impl.customization;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import com.monkey.ultimatebot.libs.invui.Click;
import com.monkey.ultimatebot.libs.invui.item.AbstractItem;
import com.monkey.ultimatebot.libs.invui.item.ItemBuilder;
import com.monkey.ultimatebot.libs.invui.item.ItemProvider;

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
