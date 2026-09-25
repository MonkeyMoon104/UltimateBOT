package com.monkey.ultimatebot.gui.v26_3.impl.settings;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import com.monkey.ultimatebot.libs.invui.Click;
import com.monkey.ultimatebot.libs.invui.item.AbstractItem;
import com.monkey.ultimatebot.libs.invui.item.ItemBuilder;
import com.monkey.ultimatebot.libs.invui.item.ItemProvider;

public class FollowItem extends AbstractItem {

    private final UltimateBot training;
    private final BotOptions options;
    private final @Nullable CombatItem combatItem;

    public FollowItem(UltimateBot training, BotOptions options, CombatItem combatItem) {
        this.training = training;
        this.options = options;
        this.combatItem = combatItem;
    }

    public FollowItem(UltimateBot training, BotOptions options) {
        this.training = training;
        this.options = options;
        this.combatItem = null;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        boolean status = options.isFollow();

        ItemBuilder builder = new ItemBuilder(
                MaterialCatalog.optional(training.getLangString("gui.follow-button.material", "LEAD"), Material.LEAD));
        builder.setLegacyName(ChatColorUtils.translate(training.getLangString("gui.follow-button.name")));

        var loreLines = training.getLangStringList("gui.follow-button.lore");

        for (String line : loreLines) {
            String processedLine = line.replace("%type%", status ? "ON" : "OFF");
            builder.addLegacyLoreLines(ChatColorUtils.translate(processedLine));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (!options.isChangeableFollow()) {
            String msg = training.getLangString(
                    "messages.follow-locked", "&cFollow is locked: it cannot be modified for this bot.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        boolean oldFollowStatus = options.isFollow();
        boolean combatStatus = options.isCombat();

        boolean newFollowStatus = !oldFollowStatus;

        if (!newFollowStatus && combatStatus && !options.isChangeableCombat()) {
            String msg = training.getLangString(
                    "messages.follow-lock-combat", "&cYou cannot disable follow: combat is locked to ON.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        options.setFollow(newFollowStatus);

        var managedOwnerUUID = resolveManagedOwnerUUID(player);
        training.getBotManager().updateFollow(managedOwnerUUID, newFollowStatus);

        if (!newFollowStatus && combatStatus) {
            options.setCombat(false);
            training.getBotManager().updateCombat(managedOwnerUUID, false);

            if (combatItem != null) {
                combatItem.notifyWindows();
            }
        }

        notifyWindows();
    }

    private java.util.UUID resolveManagedOwnerUUID(Player player) {
        if (options.getBotType() != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        java.util.UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
