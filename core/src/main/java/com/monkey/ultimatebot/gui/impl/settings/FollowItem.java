package com.monkey.ultimatebot.gui.impl.settings;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.event.BotSettingEvents;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

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
    public ItemProvider getItemProvider() {
        boolean status = options.isFollow();

        ItemBuilder builder = new ItemBuilder(MaterialCatalog.optional(
                training.getLangString("gui.follow-button.material", "LEAD"), Material.LEAD));
        builder.setDisplayName(ChatColorUtils.translate(training.getLangString("gui.follow-button.name")));

        java.util.List<String> loreLines = training.getLangStringList("gui.follow-button.lore");

        for (String line : loreLines) {
            String processedLine = line.replace("%type%", status ? "ON" : "OFF");
            builder.addLoreLines(ChatColorUtils.translate(processedLine));
        }
        return builder;
    }

    @Override
    public void handleClick(
            @NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
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

        java.util.UUID managedOwnerUUID = resolveManagedOwnerUUID(player);
        java.util.Optional<Boolean> proposedFollow = BotSettingEvents.propose(
                training,
                managedOwnerUUID,
                BotEventSource.GUI,
                BotSettingKey.FOLLOW,
                oldFollowStatus,
                newFollowStatus,
                Boolean.class);
        if (!proposedFollow.isPresent()) return;
        newFollowStatus = proposedFollow.get();

        java.util.Optional<Boolean> proposedCombat = java.util.Optional.empty();
        if (!newFollowStatus && combatStatus) {
            proposedCombat = BotSettingEvents.propose(
                    training, managedOwnerUUID, BotEventSource.GUI, BotSettingKey.COMBAT, true, false, Boolean.class);
            if (!proposedCombat.isPresent()) return;
        }

        options.setFollow(newFollowStatus);
        training.getBotManager().updateFollow(managedOwnerUUID, newFollowStatus);

        if (!newFollowStatus && combatStatus) {
            options.setCombat(proposedCombat.get());
            training.getBotManager().updateCombat(managedOwnerUUID, proposedCombat.get());

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
