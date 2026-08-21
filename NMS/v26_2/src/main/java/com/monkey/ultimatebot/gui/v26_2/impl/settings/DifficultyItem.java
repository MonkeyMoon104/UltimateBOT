package com.monkey.ultimatebot.gui.v26_2.impl.settings;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import com.monkey.ultimatebot.libs.invui.Click;
import com.monkey.ultimatebot.libs.invui.item.AbstractItem;
import com.monkey.ultimatebot.libs.invui.item.ItemBuilder;
import com.monkey.ultimatebot.libs.invui.item.ItemProvider;

public class DifficultyItem extends AbstractItem {

    private final UltimateBot training;
    private final BotOptions options;
    private final Runnable refreshDependents;

    public DifficultyItem(UltimateBot training, BotOptions options) {
        this(training, options, () -> {});
    }

    public DifficultyItem(UltimateBot training, BotOptions options, Runnable refreshDependents) {
        this.training = Objects.requireNonNull(training, "training");
        this.options = Objects.requireNonNull(options, "options");
        this.refreshDependents = Objects.requireNonNull(refreshDependents, "refreshDependents");
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
        if (materialName != null && !materialName.isBlank()) {
            Material matched = MaterialCatalog.optional(materialName, Material.AIR);
            if (matched != Material.AIR) {
                return matched;
            }
            training.getLogger()
                    .warning("Invalid material '" + materialName + "' for difficulty " + difficulty.name()
                            + " in config. Using fallback");
        }
        return MaterialCatalog.optional(
                training.getLangString("gui.difficulty-button.material", "DIAMOND_SWORD"), Material.DIAMOND_SWORD);
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
        refreshDependents.run();
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
