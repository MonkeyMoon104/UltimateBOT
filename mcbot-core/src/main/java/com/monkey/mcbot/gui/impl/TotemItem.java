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
import com.monkey.mcbot.api.event.BotEventSource;
import com.monkey.mcbot.api.event.BotSettingKey;
import com.monkey.mcbot.event.BotSettingEvents;

public class TotemItem extends AbstractItem {

    private final BotOptions options;
    private final MinecraftBot training;

    public TotemItem(BotOptions options, MinecraftBot training) {
        this.options = options;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder builder = new ItemBuilder(Material.valueOf(training.getLangString("gui.totem-button.material")));

        String unlimitedText = training.getLangString("gui.totem-button.unlimited-text");
        String countLine = options.getTotems() == -1
                ? unlimitedText
                : String.valueOf(options.getTotems());
        builder.setDisplayName(ChatColorUtils.translate(training.getLangString("gui.totem-button.name")));
        var loreLines = training.getLangStringList("gui.totem-button.lore");

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
            String msg = training.getLangString("messages.totem-locked", "&cTotems are locked: they cannot be modified for this bot.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        int maxTotem = options.getMaxTotemCount();
        int minTotem = options.getMinTotemCount();
        int currentTotem = options.getTotems();

        int nextTotem = currentTotem;
        if (clickType.isLeftClick() && currentTotem < maxTotem) nextTotem++;
        if (clickType.isRightClick() && currentTotem > minTotem) nextTotem--;
        if (nextTotem == currentTotem) return;
        UUID managedOwnerUUID = resolveManagedOwnerUUID(player);
        var proposed = BotSettingEvents.propose(training, managedOwnerUUID, BotEventSource.GUI,
                BotSettingKey.TOTEM_COUNT, currentTotem, nextTotem, Integer.class);
        if (training.getBotRegistry().getBot(managedOwnerUUID) != null && proposed.isEmpty()) return;
        if (proposed.isPresent()) nextTotem = proposed.get();
        options.setTotems(nextTotem);
        training.getBotManager().updateTotem(managedOwnerUUID, nextTotem);

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
