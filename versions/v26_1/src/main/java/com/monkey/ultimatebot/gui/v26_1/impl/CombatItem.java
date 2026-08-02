package com.monkey.ultimatebot.gui.v26_1.impl;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class CombatItem extends AbstractItem {

    private final UltimateBot training;
    private final BotOptions options;

    public CombatItem(UltimateBot training, BotOptions options) {
        this.training = training;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        boolean status = options.isCombat();

        ItemBuilder builder = new ItemBuilder(Material.valueOf(training.getLangString("gui.combat-button.material")));
        builder.setLegacyName(ChatColorUtils.translate(training.getLangString("gui.combat-button.name")));
        var loreLines = training.getLangStringList("gui.combat-button.lore");

        for (String line : loreLines) {
            String processedLine = line.replace("%type%", status ? "ON" : "OFF")
                    .replace("%difficulty%", options.getDifficulty().name());
            builder.addLegacyLoreLines(ChatColorUtils.translate(processedLine));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        UUID managedOwnerUUID = resolveManagedOwnerUUID(player);

        if (clickType.isLeftClick()) {
            if (!options.isChangeableCombat()) {
                String msg = training.getLangString(
                        "messages.combat-locked", "&cCombat is locked: it cannot be modified for this bot.");
                player.sendMessage(ChatColorUtils.translate(msg));
                return;
            }

            boolean oldStatus = options.isCombat();
            boolean newStatus = !oldStatus;

            if (newStatus && !options.isFollow()) {
                String msg = training.getLangString(
                        "messages.combat-need-follow", "&cFollow must be ON to enable bot combat");
                player.sendMessage(ChatColorUtils.translate(msg));
                return;
            }

            options.setCombat(newStatus);
            training.getBotManager().updateCombat(managedOwnerUUID, newStatus);

            if (newStatus) {
                training.getBotManager().switchBotToEnderpearl(managedOwnerUUID);
                training.getWrapperManager().active().runEntityLater(player, 1L, () -> {
                    training.getBotManager().switchBotToSword(managedOwnerUUID);
                });
            } else {
                if (training.getBotManager().getSwordSlot(managedOwnerUUID)) {
                    int totemcount = options.getTotems();
                    if (totemcount > 1) {
                        training.getBotManager().switchBotToEnderpearl(managedOwnerUUID);
                    } else {
                        training.getBotManager().switchBotToEmpty(managedOwnerUUID);
                    }
                }
            }

            player.sendMessage(ChatColorUtils.translate("&eCombat " + (newStatus ? "&aON" : "&cOFF")));
        }

        if (clickType.isRightClick()) {
            if (!options.isChangeableDifficulty()) {
                String msg = training.getLangString(
                        "messages.difficulty-locked", "&cDifficulty is locked: it cannot be modified for this bot.");
                player.sendMessage(ChatColorUtils.translate(msg));
                return;
            }

            DifficultyLevel currentDifficulty = options.getDifficulty();
            DifficultyLevel newDifficulty = options.nextAllowedDifficulty(currentDifficulty, true);
            options.setDifficulty(newDifficulty);

            training.getBotManager().setDifficultyLevel(managedOwnerUUID, newDifficulty);

            player.sendMessage(ChatColorUtils.translate("&aDifficulty set to &e" + newDifficulty.name()));
        }

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
