package com.monkey.mcbot.gui.v26_1.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.List;
import java.util.UUID;

public class RankItem extends AbstractItem {

    private final MinecraftBot training;
    private final BotOptions options;

    public RankItem(MinecraftBot training, BotOptions options) {
        this.training = training;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        BotRank currentRank = options.getRank();

        Material rankMaterial = getRankMaterial(currentRank);

        ItemBuilder builder = new ItemBuilder(rankMaterial);
        builder.setLegacyName(ChatColorUtils.translate(training.getLangString("gui.rank-button.name")));
        var loreLines = training.getLangStringList("gui.rank-button.lore");
        List<String> ranks = options.getAllowedRanks().stream()
                .map(botRank -> botRank == options.getRank() ? botRank.getSelectedName() : botRank.getDisplayName())
                .toList();

        for (String line : loreLines) {
            if (line.contains("%ranks%")) {
                for (String rank : ranks) {
                    builder.addLegacyLoreLines(ChatColorUtils.translate("- " + rank));
                }
            } else {
                String processedLine = line.replace("%ranks%", "");
                builder.addLegacyLoreLines(ChatColorUtils.translate(processedLine));
            }
        }
        return builder;
    }

    private Material getRankMaterial(BotRank rank) {
        String configPath = "gui.rank-button.ranks-mat." + rank.name().toLowerCase();
        String materialName = training.getLangString(configPath);

        if (materialName != null) {
            try {
                return Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                training.getLogger().warning("Invalid material '" + materialName + "' for rank " + rank.name() + " in config. Using fallback");
            }
        }

        String defaultMaterial = training.getLangString("gui.rank-button.material", "DIAMOND_SWORD");
        try {
            return Material.valueOf(defaultMaterial.toUpperCase());
        } catch (IllegalArgumentException e) {
            training.getLogger().warning("Invalid fallback material '" + defaultMaterial + "' in config. Using DIAMOND_SWORD");
            return Material.DIAMOND_SWORD;
        }
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (!options.isChangeableRank()) {
            String msg = training.getLangString("messages.rank-locked", "&cRank is locked: it cannot be modified for this bot.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        BotRank currentRank = options.getRank();
        BotRank newRank = currentRank;

        if (clickType == ClickType.LEFT) {
            newRank = options.nextAllowedRank(currentRank, true);
        } else if (clickType == ClickType.RIGHT) {
            newRank = options.nextAllowedRank(currentRank, false);
        }

        options.setRank(newRank);

        training.getBotManager().setBotRank(resolveManagedOwnerUUID(player), newRank);

        player.sendMessage(ChatColorUtils.translate("&aRank set to &e" + newRank.getSelectedName()));
        notifyWindows();
    }

    private UUID resolveManagedOwnerUUID(Player player) {
        if (options.getBotType() != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}



