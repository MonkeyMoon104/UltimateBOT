package com.monkey.ultimatebot.gui.v26_2.impl.action;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import com.monkey.ultimatebot.libs.invui.Click;
import com.monkey.ultimatebot.libs.invui.item.AbstractItem;
import com.monkey.ultimatebot.libs.invui.item.ItemBuilder;
import com.monkey.ultimatebot.libs.invui.item.ItemProvider;

public class TeleportItem extends AbstractItem {

    private final UltimateBot training;

    public TeleportItem(UltimateBot training) {
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        Material mat = MaterialCatalog.optional(
                training.getLangString("gui.teleport-button.material", "ENDER_PEARL"), Material.ENDER_PEARL);
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

        NMSBridgeManager.get().moveBot(bot.asBukkitPlayer(), player.getX(), player.getY(), player.getZ());
    }

    private UUID resolveBotOwnerUUID(Player player) {
        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
