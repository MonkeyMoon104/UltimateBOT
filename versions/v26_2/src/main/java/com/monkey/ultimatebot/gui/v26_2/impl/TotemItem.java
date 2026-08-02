package com.monkey.ultimatebot.gui.v26_2.impl;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class TotemItem extends AbstractItem {

    private final BotOptions options;
    private final UltimateBot training;

    public TotemItem(BotOptions options, UltimateBot training) {
        this.options = options;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        ItemBuilder builder = new ItemBuilder(Material.valueOf(training.getLangString("gui.totem-button.material")));

        String unlimitedText = Objects.requireNonNull(
                training.getLangString("gui.totem-button.unlimited-text"), "gui.totem-button.unlimited-text");
        String countLine = options.getTotems() == -1 ? unlimitedText : String.valueOf(options.getTotems());
        builder.setLegacyName(ChatColorUtils.translate(training.getLangString("gui.totem-button.name")));
        var loreLines = training.getLangStringList("gui.totem-button.lore");

        for (String line : loreLines) {
            String replaced = line.replace("%count%", countLine);
            builder.addLegacyLoreLines(ChatColorUtils.translate(replaced));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (!options.isChangeableTotem()) {
            String msg = training.getLangString(
                    "messages.totem-locked", "&cTotems are locked: they cannot be modified for this bot.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        int maxTotem = options.getMaxTotemCount();
        int minTotem = options.getMinTotemCount();
        int currentTotem = options.getTotems();

        if (clickType.isLeftClick() && currentTotem < maxTotem) {
            options.setTotems(currentTotem + 1);
        }

        if (clickType.isRightClick() && currentTotem > minTotem) {
            options.setTotems(currentTotem - 1);
        }

        training.getBotManager().updateTotem(resolveManagedOwnerUUID(player), options.getTotems());

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
