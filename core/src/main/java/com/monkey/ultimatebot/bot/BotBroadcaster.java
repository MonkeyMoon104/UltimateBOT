package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.utils.Packet;
import com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils;
import java.util.Collection;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class BotBroadcaster {

    public static void broadcastSpawn(
            ITrainingBot bot,
            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            showBotToViewer(online, bot);
        }
        BotEquipmentUtils.broadcastEquipment(bot, armorMap, blastProtectionMap);
    }

    public static int syncVisibleBotsForPlayer(Player viewer, Collection<ITrainingBot> bots) {
        if (viewer == null || !viewer.isOnline()) {
            return 0;
        }

        int sent = 0;
        World viewerWorld = viewer.getWorld();

        for (ITrainingBot bot : bots) {
            if (bot == null || bot.asBukkitPlayer() == null || bot.asBukkitPlayer().getWorld() == null) {
                continue;
            }

            World botWorld = bot.asBukkitPlayer().getWorld();
            if (botWorld == null || !viewerWorld.getUID().equals(botWorld.getUID())) {
                continue;
            }

            showBotToViewer(viewer, bot);
            sent++;
        }

        return sent;
    }

    private static void showBotToViewer(Player viewer, ITrainingBot bot) {
        Packet.sendAddPlayerPacket(viewer, bot);
        Packet.sendSpawnPlayerPacket(viewer, bot);
        BotEquipmentUtils.sendCurrentEquipmentToViewer(bot, viewer);
        // Classic NPC flow: ADD_PLAYER is required for skins on older clients, then REMOVE_PLAYER
        // hides the entry from the tab list while keeping the spawned entity visible.
        UltimateBot plugin = bot.getPlugin();
        if (plugin != null) {
            Bukkit.getScheduler()
                    .runTaskLater(
                            plugin,
                            () -> {
                                if (viewer.isOnline() && !bot.isRemoved()) {
                                    Packet.sendRemovePlayerPacket(viewer, bot);
                                }
                            },
                            2L);
        }
    }

    public static void broadcastDespawn(ITrainingBot bot) {
        if (bot == null) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            Packet.sendRemovePlayerPacket(online, bot);
        }
    }
}
