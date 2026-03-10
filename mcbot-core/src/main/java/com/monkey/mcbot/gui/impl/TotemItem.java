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

import java.util.UUID;

public class TotemItem extends AbstractItem {

    private final BotOptions options;
    private final MinecraftBot training;

    public TotemItem(BotOptions options, MinecraftBot training) {
        this.options = options;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder builder = new ItemBuilder(Material.valueOf(training.getConfig().getString("gui.totem-button.material")));

        String unlimitedText = training.getConfig().getString("gui.totem-button.unlimited-text");
        String countLine = options.getTotems() == -1
                ? unlimitedText
                : String.valueOf(options.getTotems());
        builder.setDisplayName(ChatColorUtils.translate(training.getConfig().getString("gui.totem-button.name")));
        var loreLines = training.getConfig().getStringList("gui.totem-button.lore");

        for (String line : loreLines) {
            assert countLine != null;
            String replaced = line.replace("%count%", countLine);
            builder.addLoreLines(ChatColorUtils.translate(replaced));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        if (!options.isChangeableTotem()) {
            String msg = training.getConfig().getString("messages.totem-locked", "&cTotem bloccato: non modificabile per questo bot.");
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
