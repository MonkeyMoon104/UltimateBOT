package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class FollowItem extends AbstractItem {

    private final MinecraftBot training;
    private final BotOptions options;
    private final CombatItem combatItem;

    public FollowItem(MinecraftBot training, BotOptions options, CombatItem combatItem) {
        this.training = training;
        this.options = options;
        this.combatItem = combatItem;
    }

    public FollowItem(MinecraftBot training, BotOptions options) {
        this.training = training;
        this.options = options;
        this.combatItem = null;
    }

    @Override
    public ItemProvider getItemProvider() {
        boolean status = options.isFollow();

        ItemBuilder builder = new ItemBuilder(Material.valueOf(training.getConfig().getString("gui.follow-button.material")));
        builder.setDisplayName(ChatColorUtils.translate(training.getConfig().getString("gui.follow-button.name")));

        var loreLines = training.getConfig().getStringList("gui.follow-button.lore");

        for (String line : loreLines) {
            String processedLine = line.replace("%type%", status ? "ON" : "OFF");
            builder.addLoreLines(ChatColorUtils.translate(processedLine));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        boolean oldFollowStatus = options.isFollow();
        boolean combatStatus = options.isCombat();

        boolean newFollowStatus = !oldFollowStatus;
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