package com.monkey.ultimatebot.gui.v26_1.impl;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class DifficultyItem extends AbstractItem {

    private final UltimateBot training;
    private final BotOptions options;

    public DifficultyItem(UltimateBot training, BotOptions options) {
        this.training = training;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        DifficultyLevel currentDifficulty = options.getDifficulty();

        Material difficultyMaterial = getDifficultyMaterial(currentDifficulty);

        ItemBuilder builder = new ItemBuilder(difficultyMaterial);
        builder.setLegacyName(ChatColorUtils.translate(training.getLangString("gui.difficulty-button.name")));
        var loreLines = training.getLangStringList("gui.difficulty-button.lore");
        List<String> difficulties = options.getAllowedDifficulties().stream()
                .map(botDifficulty -> botDifficulty == options.getDifficulty()
                        ? botDifficulty.getSelectedName()
                        : botDifficulty.getDisplayName())
                .toList();

        for (String line : loreLines) {
            if (line.contains("%difficulties%")) {
                for (String difficulty : difficulties) {
                    builder.addLegacyLoreLines(ChatColorUtils.translate("- " + difficulty));
                }
            } else {
                String processedLine = line.replace("%difficulties%", "");
                builder.addLegacyLoreLines(ChatColorUtils.translate(processedLine));
            }
        }
        return builder;
    }

    private Material getDifficultyMaterial(DifficultyLevel difficulty) {
        String configPath =
                "gui.difficulty-button.difficulties-mat." + difficulty.name().toLowerCase(Locale.ROOT);
        String materialName = training.getLangString(configPath);

        if (materialName != null) {
            try {
                return Material.valueOf(materialName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                training.getLogger()
                        .warning("Invalid material '" + materialName + "' for difficulty " + difficulty.name()
                                + " in config. Using fallback");
            }
        }

        String defaultMaterial = training.getLangString("gui.difficulty-button.material", "DIAMOND_SWORD");
        try {
            return Material.valueOf(defaultMaterial.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            training.getLogger()
                    .warning("Invalid fallback material '" + defaultMaterial + "' in config. Using DIAMOND_SWORD");
            return Material.DIAMOND_SWORD;
        }
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (!options.isChangeableDifficulty()) {
            String msg = training.getLangString(
                    "messages.difficulty-locked", "&cDifficulty is locked: it cannot be modified for this bot.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        DifficultyLevel currentDifficulty = options.getDifficulty();
        DifficultyLevel newDifficulty = currentDifficulty;

        if (clickType == ClickType.LEFT) {
            newDifficulty = options.nextAllowedDifficulty(currentDifficulty, true);
        } else if (clickType == ClickType.RIGHT) {
            newDifficulty = options.nextAllowedDifficulty(currentDifficulty, false);
        }

        options.setDifficulty(newDifficulty);

        training.getBotManager().setDifficultyLevel(resolveManagedOwnerUUID(player), newDifficulty);

        player.sendMessage(ChatColorUtils.translate("&aDifficulty set to &e" + newDifficulty.getSelectedName()));
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
