package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class TrimBackgroundItem extends AbstractItem {

    private final MinecraftBot training;
    private final BotOptions options;
    private final EquipmentSlot slot;

    public TrimBackgroundItem(MinecraftBot training, BotOptions options, EquipmentSlot slot) {
        this.training = training;
        this.options = options;
        this.slot = slot;
    }

    @Override
    public ItemProvider getItemProvider() {
        Material material = Material.matchMaterial(training.getLangString(
                "gui.templates-background.material",
                "LIGHT_GRAY_STAINED_GLASS_PANE"
        ));
        if (material == null) {
            material = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
        }

        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(ChatColorUtils.translate(training.getLangString(
                "gui.templates-background.name",
                "&7Background"
        )));

        for (String line : training.getLangStringList("gui.templates-background.lore")) {
            builder.addLoreLines(ChatColorUtils.translate(line));
        }

        return builder;
    }

    @Override
    public void handleClick(org.bukkit.event.inventory.ClickType clickType, Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
    }
}
