package com.monkey.mcbot.gui.v26_1.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.nms.NMSBridgeManager;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.UUID;

public class TeleportItem extends AbstractItem {

    private final MinecraftBot training;

    public TeleportItem(MinecraftBot training) {
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        Material mat = Material.valueOf(training.getLangString("gui.teleport-button.material"));
        String name = training.getLangString("gui.teleport-button.name");
        var lore = training.getLangStringList("gui.teleport-button.lore");

        ItemBuilder builder = new ItemBuilder(mat);
        builder.setLegacyName(ChatColorUtils.translate(name));
        for (String line : lore) {
            builder.addLegacyLoreLines(ChatColorUtils.translate(line));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
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


