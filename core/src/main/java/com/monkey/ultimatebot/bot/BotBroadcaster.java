package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess;
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
            Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> armorMap,
            Map<EquipmentSlotKind, Boolean> blastProtectionMap) {
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
            if (bot == null
                    || bot.asBukkitPlayer() == null
                    || bot.asBukkitPlayer().getWorld() == null) {
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

    private static long tabListRemoveDelayTicks() {
        return MinecraftVersionAccess.isAtLeast(1, 12) ? 2L : 40L;
    }

    private static void showBotToViewer(Player viewer, ITrainingBot bot) {
        Packet.sendAddPlayerPacket(viewer, bot);
        UltimateBot plugin = bot.getPlugin();
        Runnable spawnAndEquip = () -> {
            if (!viewer.isOnline() || bot.isRemoved()) {
                return;
            }
            Packet.sendSpawnPlayerPacket(viewer, bot);
            BotEquipmentUtils.sendCurrentEquipmentToViewer(bot, viewer);
        };
        if (plugin == null) {
            spawnAndEquip.run();
            return;
        }

        long spawnDelay = MinecraftVersionAccess.isAtLeast(1, 12) ? 0L : 1L;
        if (spawnDelay <= 0L) {
            spawnAndEquip.run();
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, spawnAndEquip, spawnDelay);
        }
        Bukkit.getScheduler()
                .runTaskLater(
                        plugin,
                        () -> {
                            if (viewer.isOnline() && !bot.isRemoved()) {
                                Packet.sendRemovePlayerPacket(viewer, bot);
                            }
                        },
                        tabListRemoveDelayTicks());
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
