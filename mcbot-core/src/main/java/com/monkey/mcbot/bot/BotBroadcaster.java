package com.monkey.mcbot.bot;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.utils.Packet;
import com.monkey.mcbot.utils.equipment.BotEquipmentUtils;
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
        BotEquipmentUtils.broadcastEquipment(bot.asPlayer(), armorMap, blastProtectionMap);
    }

    public static int syncVisibleBotsForPlayer(Player viewer, Collection<ITrainingBot> bots) {
        if (viewer == null || !viewer.isOnline()) {
            return 0;
        }

        int sent = 0;
        World viewerWorld = viewer.getWorld();

        for (ITrainingBot bot : bots) {
            if (bot == null || bot.asPlayer() == null || bot.asPlayer().level() == null) {
                continue;
            }

            World botWorld = bot.asPlayer().level().getWorld();
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
        BotEquipmentUtils.sendCurrentEquipmentToViewer(bot.asPlayer(), viewer);
    }
}
