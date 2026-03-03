package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Arrays;
import java.util.List;

public class RankItem extends AbstractItem {

    private final SandboxTraining training;
    private final BotOptions options;

    public RankItem(SandboxTraining training, BotOptions options) {
        this.training = training;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider() {
        BotRank currentRank = options.getRank();

        Material rankMaterial = getRankMaterial(currentRank);

        ItemBuilder builder = new ItemBuilder(rankMaterial);
        builder.setDisplayName(ChatColorUtils.translate(training.getConfig().getString("gui.rank-button.name")));
        builder.setItemFlags(List.of(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES));

        var loreLines = training.getConfig().getStringList("gui.rank-button.lore");
        List<String> ranks = Arrays.stream(BotRank.values())
                .map(botRank -> botRank == options.getRank() ? botRank.getSelectedName() : botRank.getDisplayName())
                .toList();

        for (String line : loreLines) {
            if (line.contains("%ranks%")) {
                for (String rank : ranks) {
                    builder.addLoreLines(ChatColorUtils.translate("- " + rank));
                }
            } else {
                String processedLine = line.replace("%ranks%", "");
                builder.addLoreLines(ChatColorUtils.translate(processedLine));
            }
        }
        return builder;
    }

    private Material getRankMaterial(BotRank rank) {
        String configPath = "gui.rank-button.ranks-mat." + rank.name().toLowerCase();
        String materialName = training.getConfig().getString(configPath);

        if (materialName != null) {
            try {
                return Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                training.getLogger().warning("Invalid material '" + materialName + "' for rank " + rank.name() + " in config. Using fallback");
            }
        }

        String defaultMaterial = training.getConfig().getString("gui.rank-button.material", "DIAMOND_SWORD");
        try {
            return Material.valueOf(defaultMaterial.toUpperCase());
        } catch (IllegalArgumentException e) {
            training.getLogger().warning("Invalid fallback material '" + defaultMaterial + "' in config. Using DIAMOND_SWORD");
            return Material.DIAMOND_SWORD;
        }
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        BotRank currentRank = options.getRank();
        BotRank[] values = BotRank.values();
        int index = currentRank.ordinal();

        if (clickType == ClickType.LEFT) {
            index = (index + 1) % values.length;
        } else if (clickType == ClickType.RIGHT) {
            index = (index - 1 + values.length) % values.length;
        }

        BotRank newRank = values[index];
        options.setRank(newRank);

        training.getBotManager().setBotRank(player.getUniqueId(), newRank);

        player.sendMessage(ChatColorUtils.translate("&aRank impostato su &e" + newRank.getSelectedName()));
        notifyWindows();
    }
}