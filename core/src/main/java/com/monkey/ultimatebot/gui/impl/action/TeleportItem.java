package com.monkey.ultimatebot.gui.impl.action;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class TeleportItem extends AbstractItem {

    private final UltimateBot training;

    public TeleportItem(UltimateBot training) {
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider() {
        Material mat = Material.valueOf(training.getLangString("gui.teleport-button.material"));
        String name = training.getLangString("gui.teleport-button.name");
        var lore = training.getLangStringList("gui.teleport-button.lore");

        ItemBuilder builder = new ItemBuilder(mat);
        builder.setDisplayName(ChatColorUtils.translate(name));
        for (String line : lore) {
            builder.addLoreLines(ChatColorUtils.translate(line));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        UUID botOwnerUUID = resolveBotOwnerUUID(player);
        if (botOwnerUUID == null || !training.getBotManager().isBotSpawned(botOwnerUUID)) {
            return;
        }

        ITrainingBot bot = training.getBotManager().getBot(botOwnerUUID);
        if (bot == null) {
            return;
        }

        NMSBridgeManager.get().moveBot(bot.asPlayer(), player.getX(), player.getY(), player.getZ());
    }

    private UUID resolveBotOwnerUUID(Player player) {
        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
