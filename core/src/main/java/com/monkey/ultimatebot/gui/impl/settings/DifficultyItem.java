package com.monkey.ultimatebot.gui.impl.settings;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.event.BotSettingEvents;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.item.ItemFlagCatalog;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class DifficultyItem extends AbstractItem {

    private final UltimateBot training;
    private final BotOptions options;
    private final Runnable difficultyChanged;

    public DifficultyItem(UltimateBot training, BotOptions options) {
        this(training, options, () -> {});
    }

    public DifficultyItem(UltimateBot training, BotOptions options, Runnable difficultyChanged) {
        this.training = Objects.requireNonNull(training, "training");
        this.options = Objects.requireNonNull(options, "options");
        this.difficultyChanged = Objects.requireNonNull(difficultyChanged, "difficultyChanged");
    }

    @Override
    public ItemProvider getItemProvider() {
        DifficultyLevel currentDifficulty = options.getDifficulty();

        Material difficultyMaterial = getDifficultyMaterial(currentDifficulty);

        ItemBuilder builder = new ItemBuilder(difficultyMaterial);
        builder.setDisplayName(ChatColorUtils.translate(training.getLangString("gui.difficulty-button.name")));
        builder.setItemFlags(ItemFlagCatalog.resolve("HIDE_ADDITIONAL_TOOLTIP", "HIDE_ENCHANTS", "HIDE_ATTRIBUTES"));

        java.util.List<String> loreLines = training.getLangStringList("gui.difficulty-button.lore");
        List<String> difficulties = options.getAllowedDifficulties().stream()
                .map(botDifficulty -> botDifficulty == options.getDifficulty()
                        ? botDifficulty.getSelectedName()
                        : botDifficulty.getDisplayName())
                .collect(Collectors.toList());

        for (String line : loreLines) {
            if (line.contains("%difficulties%")) {
                for (String difficulty : difficulties) {
                    builder.addLoreLines(ChatColorUtils.translate("- " + difficulty));
                }
            } else {
                String processedLine = line.replace("%difficulties%", "");
                builder.addLoreLines(ChatColorUtils.translate(processedLine));
            }
        }
        return builder;
    }

    private Material getDifficultyMaterial(DifficultyLevel difficulty) {
        String configPath =
                "gui.difficulty-button.difficulties-mat." + difficulty.name().toLowerCase(Locale.ROOT);
        String materialName = training.getLangString(configPath);
        if (materialName != null && !materialName.trim().isEmpty()) {
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
    public void handleClick(
            @NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
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

        UUID managedOwnerUUID = resolveManagedOwnerUUID(player);
        java.util.Optional<com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel> proposed =
                BotSettingEvents.propose(
                        training,
                        managedOwnerUUID,
                        BotEventSource.GUI,
                        BotSettingKey.DIFFICULTY,
                        currentDifficulty,
                        newDifficulty,
                        DifficultyLevel.class);
        if (!proposed.isPresent()) return;
        newDifficulty = proposed.get();
        options.setDifficulty(newDifficulty);

        training.getBotManager().setDifficultyLevel(managedOwnerUUID, newDifficulty);

        player.sendMessage(ChatColorUtils.translate("&aDifficulty set to &e" + newDifficulty.getSelectedName()));
        difficultyChanged.run();
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
